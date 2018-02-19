package bio.fkaiser.mmm.model.metrics;

import bio.fkaiser.mmm.model.Distribution;
import bio.fkaiser.mmm.model.Itemset;
import bio.fkaiser.mmm.model.configurations.metrics.ConsensusMetricConfiguration;
import de.bioforscher.singa.structure.algorithms.superimposition.consensus.ConsensusAlignment;
import de.bioforscher.singa.structure.algorithms.superimposition.consensus.ConsensusBuilder;
import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.representations.RepresentationSchemeType;
import de.bioforscher.singa.structure.model.interfaces.Atom;
import de.bioforscher.singa.structure.model.oak.StructuralMotif;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * An {@link ExtractionMetric} that extracts {@link Itemset}s based on the adherence of their items. For further reference see
 * <p/>
 * Kaiser, F. & Labudde, D. IEEE/ACM Trans. Comput. Biol. Bioinform. (under review)
 *
 * @author fk
 */
public class ConsensusMetric<LabelType extends Comparable<LabelType>> extends AbstractExtractionDependentMetric<LabelType> implements ParallelizableMetric<LabelType>, DistributionMetric<LabelType> {

    public static final Comparator<Itemset<?>> COMPARATOR = Comparator.comparing(Itemset::getConsensus);

    private static final Logger logger = LoggerFactory.getLogger(ConsensusMetric.class);

    private final Map<Itemset<LabelType>, ConsensusAlignment> clusteredItemsets;
    private final double maximalConsensus;
    private final double clusterCutoff;
    private final Predicate<Atom> atomFilter;
    private final int levelOfParallelism;
    private final ExecutorService executorService;
    private final boolean alignWithinClusters;
    private final RepresentationSchemeType representationSchemeType;
    private Map<Itemset<LabelType>, Distribution> distributions;

    public ConsensusMetric(ConsensusMetricConfiguration<LabelType> consensusMetricConfiguration) {
        maximalConsensus = consensusMetricConfiguration.getMaximalConsensus();
        clusterCutoff = consensusMetricConfiguration.getClusterCutoffValue();
        levelOfParallelism = consensusMetricConfiguration.getLevelOfParallelism();
        executorService = (levelOfParallelism == -1) ? Executors.newWorkStealingPool() : Executors.newWorkStealingPool(levelOfParallelism);
        atomFilter = consensusMetricConfiguration.getAtomFilterType().getFilter();
        representationSchemeType = consensusMetricConfiguration.getRepresentationSchemeType();
        alignWithinClusters = consensusMetricConfiguration.isAlignWithinClusters();

        distributions = new HashMap<>();
        clusteredItemsets = new HashMap<>();
    }

    /**
     * Calculates the consensus score for a given {@link ConsensusAlignment}.
     *
     * @param consensusAlignment The {@link ConsensusAlignment} for which the score should be calculated.
     * @return The consensus score.
     */
    public static double calculateConsensus(ConsensusAlignment consensusAlignment) {
        return consensusAlignment.getNormalizedConsensusScore();
    }

    public Map<Itemset<LabelType>, ConsensusAlignment> getClusteredItemsets() {
        return clusteredItemsets;
    }

    @Override
    public int getMinimalItemsetSize() {
        return 2;
    }

    @Override
    public Set<Itemset<LabelType>> filterItemsets(Set<Itemset<LabelType>> itemsets, Map<Itemset<LabelType>, List<Itemset<LabelType>>> extractedItemsets) {
        this.extractedItemsets = extractedItemsets;
        return calculateConsensus(itemsets).stream()
                                           .filter(itemset -> itemset.getConsensus() <= maximalConsensus)
                                           .collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return "ConsensusMetric{" +
               "maximalConsensus=" + maximalConsensus +
               '}';
    }

    private Set<Itemset<LabelType>> calculateConsensus(Set<Itemset<LabelType>> itemsets) {

        // create chunks for parallel execution
        List<List<Itemset<LabelType>>> partitions = partition(new ArrayList<>(itemsets), (levelOfParallelism == -1) ? AVAILABLE_PROCESSORS : levelOfParallelism);

        // create jobs
        List<ConsensusCalculator> jobs = partitions.stream()
                                                   .map(ConsensusCalculator::new)
                                                   .collect(Collectors.toList());
        // execute jobs
        try {
            executorService.invokeAll(jobs).stream()
                           .map(future -> {
                               try {
                                   return future.get();
                               } catch (InterruptedException | ExecutionException e) {
                                   throw new IllegalStateException(e);
                               }
                           })
                           .collect(Collectors.toList())
                           .forEach(clusteredItemsets::putAll);
        } catch (InterruptedException e) {
            logger.error("parallel execution of {} failed", this, e);
        }

        return itemsets;
    }

    @Override
    public void filterExtractedItemsets() {
        extractedItemsets.entrySet().removeIf(entry -> entry.getKey().getConsensus() > maximalConsensus);
    }

    @Override public Map<Itemset<LabelType>, Distribution> getDistributions() {
        return distributions;
    }

    private class ConsensusCalculator implements Callable<Map<Itemset<LabelType>, ConsensusAlignment>> {
        private final List<Itemset<LabelType>> itemsets;

        public ConsensusCalculator(List<Itemset<LabelType>> itemsets) {
            this.itemsets = itemsets;
        }

        @Override
        public Map<Itemset<LabelType>, ConsensusAlignment> call() throws Exception {
            Map<Itemset<LabelType>, ConsensusAlignment> clusteredItemsets = new HashMap<>();
            for (Itemset<LabelType> itemset : itemsets) {

                // get structural motifs for current itemset
                List<StructuralMotif> structuralMotifs = extractedItemsets.get(itemset).stream()
                                                                          .map(Itemset::getStructuralMotif)
                                                                          .filter(Optional::isPresent)
                                                                          .map(Optional::get)
                                                                          .collect(Collectors.toList());

                // perform consensus alignment
                ConsensusAlignment consensusAlignment;
                if (representationSchemeType != null) {
                    consensusAlignment = ConsensusBuilder.create()
                                                         .inputStructuralMotifs(structuralMotifs)
                                                         .representationSchemeType(representationSchemeType)
                                                         .clusterCutoff(clusterCutoff)
                                                         .alignWithinClusters(alignWithinClusters)
                                                         .idealSuperimposition(false)
                                                         .run();
                } else {
                    consensusAlignment = ConsensusBuilder.create()
                                                         .inputStructuralMotifs(structuralMotifs)
                                                         .atomFilter(atomFilter)
                                                         .clusterCutoff(clusterCutoff)
                                                         .alignWithinClusters(alignWithinClusters)
                                                         .idealSuperimposition(false)
                                                         .run();
                }

                consensusAlignment.getAlignmentTrace().forEach(observationValue -> addObservationForItemset(itemset, observationValue));

                // store consensus score
                itemset.setConsensus(calculateConsensus(consensusAlignment));
                clusteredItemsets.put(itemset, consensusAlignment);
            }
            return clusteredItemsets;
        }
    }
}
