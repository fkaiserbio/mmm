package de.bioforscher.mmm.model.metrics;

import de.bioforscher.mmm.model.Distribution;
import de.bioforscher.mmm.model.Itemset;
import de.bioforscher.mmm.model.configurations.metrics.ConsensusMetricConfiguration;
import de.bioforscher.singa.chemistry.algorithms.superimposition.consensus.ConsensusAlignment;
import de.bioforscher.singa.chemistry.algorithms.superimposition.consensus.ConsensusBuilder;
import de.bioforscher.singa.chemistry.physical.atoms.Atom;
import de.bioforscher.singa.chemistry.physical.atoms.representations.RepresentationSchemeType;
import de.bioforscher.singa.chemistry.physical.branches.StructuralMotif;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
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

    public ConsensusMetric(ConsensusMetricConfiguration consensusMetricConfiguration) {
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

    @Override public String toString() {
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
                consensusAlignment.writeClusters(Paths.get("/tmp/test"));

                // store consensus score
                itemset.setConsensus(consensusAlignment.getNormalizedConsensusScore());
                clusteredItemsets.put(itemset, consensusAlignment);
            }
            return clusteredItemsets;
        }
    }
}
