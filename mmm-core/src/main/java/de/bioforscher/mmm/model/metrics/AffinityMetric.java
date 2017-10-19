package de.bioforscher.mmm.model.metrics;

import de.bioforscher.mmm.model.Distribution;
import de.bioforscher.mmm.model.Itemset;
import de.bioforscher.mmm.model.configurations.metrics.AffinityMetricConfiguration;
import de.bioforscher.singa.chemistry.algorithms.superimposition.affinity.AffinityAlignment;
import de.bioforscher.singa.chemistry.physical.atoms.Atom;
import de.bioforscher.singa.chemistry.physical.atoms.representations.RepresentationSchemeType;
import de.bioforscher.singa.chemistry.physical.branches.StructuralMotif;
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
 * @author fk
 */
public class AffinityMetric<LabelType extends Comparable<LabelType>> extends AbstractExtractionDependentMetric<LabelType> implements ParallelizableMetric<LabelType>, DistributionMetric<LabelType> {

    public static final Comparator<Itemset<?>> COMPARATOR = Comparator.comparing(Itemset::getAffinity);

    private static final Logger logger = LoggerFactory.getLogger(AffinityMetric.class);

    private final double maximalAffinity;
    private final int levelOfParallelism;
    private final ExecutorService executorService;
    private final RepresentationSchemeType representationSchemeType;
    private final Predicate<Atom> atomFilter;
    private final boolean alignWithinClusters;
    private final Map<Itemset<LabelType>, AffinityAlignment> affinityItemsets;

    public AffinityMetric(AffinityMetricConfiguration<LabelType> affinityMetricConfiguration) {
        maximalAffinity = affinityMetricConfiguration.getMaximalAffinity();
        levelOfParallelism = affinityMetricConfiguration.getLevelOfParallelism();
        executorService = (levelOfParallelism == -1) ? Executors.newWorkStealingPool() : Executors.newWorkStealingPool(levelOfParallelism);
        representationSchemeType = affinityMetricConfiguration.getRepresentationSchemeType();
        atomFilter = affinityMetricConfiguration.getAtomFilterType().getFilter();
        alignWithinClusters = affinityMetricConfiguration.isAlignWithinClusters();

        affinityItemsets = new HashMap<>();
    }

    /**
     * Calculates the affinity score for the given {@link AffinityAlignment}.
     *
     * @param affinityAlignment The {@link AffinityAlignment} for which the score should be calculated.
     * @return The affinity score.
     */
    public static double calculateAffinity(AffinityAlignment affinityAlignment) {
        return affinityAlignment.getSelfDissimilarity() / affinityAlignment.getClusters().size();
    }

    public Map<Itemset<LabelType>, AffinityAlignment> getAffinityItemsets() {
        return affinityItemsets;
    }

    @Override

    public Map<Itemset<LabelType>, Distribution> getDistributions() {
        return null;
    }

    @Override
    public int getMinimalItemsetSize() {
        return 2;
    }

    @Override
    public Set<Itemset<LabelType>> filterItemsets(Set<Itemset<LabelType>> itemsets, Map<Itemset<LabelType>, List<Itemset<LabelType>>> extractedItemsets) {
        this.extractedItemsets = extractedItemsets;

        return calculateAffinity(itemsets).stream()
                                          .filter(itemset -> itemset.getAffinity() <= maximalAffinity)
                                          .collect(Collectors.toSet());
    }

    private Set<Itemset<LabelType>> calculateAffinity(Set<Itemset<LabelType>> itemsets) {

        // create chunks for parallel execution
        List<List<Itemset<LabelType>>> partitions = partition(new ArrayList<>(itemsets), (levelOfParallelism == -1) ? AVAILABLE_PROCESSORS : levelOfParallelism);

        // create jobs
        List<AffinityCalculator> jobs = partitions.stream()
                                                  .map(AffinityCalculator::new)
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
                           .forEach(affinityItemsets::putAll);
        } catch (InterruptedException e) {
            logger.error("parallel execution of {} failed", this, e);
        }

        return itemsets;
    }

    @Override
    public void filterExtractedItemsets() {
        extractedItemsets.entrySet().removeIf(entry -> entry.getKey().getAffinity() >= maximalAffinity);
    }

    @Override
    public String toString() {
        return "AffinityMetric{" +
               "maximalAffinity=" + maximalAffinity +
               '}';
    }

    private class AffinityCalculator implements Callable<Map<Itemset<LabelType>, AffinityAlignment>> {
        private final List<Itemset<LabelType>> itemsets;

        public AffinityCalculator(List<Itemset<LabelType>> itemsets) {
            this.itemsets = itemsets;
        }

        @Override
        public Map<Itemset<LabelType>, AffinityAlignment> call() throws Exception {
            Map<Itemset<LabelType>, AffinityAlignment> affinityItemsets = new HashMap<>();
            for (Itemset<LabelType> itemset : itemsets) {
                // get structural motifs for current itemset
                List<StructuralMotif> structuralMotifs = extractedItemsets.get(itemset).stream()
                                                                          .map(Itemset::getStructuralMotif)
                                                                          .filter(Optional::isPresent)
                                                                          .map(Optional::get)
                                                                          .collect(Collectors.toList());

                // perform consensus alignment
                AffinityAlignment affinityAlignment;
                if (representationSchemeType != null) {
                    affinityAlignment = AffinityAlignment.create()
                                                         .inputStructuralMotifs(structuralMotifs)
                                                         .representationSchemeType(representationSchemeType)
                                                         .alignWithinClusters(alignWithinClusters)
                                                         .idealSuperimposition(false)
                                                         .run();
                } else {
                    affinityAlignment = AffinityAlignment.create()
                                                         .inputStructuralMotifs(structuralMotifs)
                                                         .atomFilter(atomFilter)
                                                         .alignWithinClusters(alignWithinClusters)
                                                         .idealSuperimposition(false)
                                                         .run();
                }

                affinityItemsets.put(itemset, affinityAlignment);

                // TODO preliminary naive computation of score for affinity
                double affinity = calculateAffinity(affinityAlignment);
                itemset.setAffinity(affinity);
            }
            return affinityItemsets;
        }
    }
}
