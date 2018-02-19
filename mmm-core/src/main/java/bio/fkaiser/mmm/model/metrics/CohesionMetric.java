package bio.fkaiser.mmm.model.metrics;

import bio.fkaiser.mmm.Itemsets;
import bio.fkaiser.mmm.model.DataPoint;
import bio.fkaiser.mmm.model.Distribution;
import bio.fkaiser.mmm.model.Itemset;
import bio.fkaiser.mmm.model.configurations.metrics.CohesionMetricConfiguration;
import bio.fkaiser.mmm.model.metrics.cohesion.CohesionMetricException;
import bio.fkaiser.mmm.model.metrics.cohesion.VertexCandidateGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * An heuristic implementation of the cohesion measurement for {@link Itemset}s. Adapted from:
 * <p/>
 * C. Zhou, P. Meysman, B.
 * Cule, K. Laukens and B. Goethals, "Discovery of Spatially Cohesive Itemsets in Three-Dimensional Protein
 * Structures," in IEEE/ACM Transactions on Computational Biology and Bioinformatics, vol. 11, no. 5, pp. 814-825,
 * Sept-Oct. 2014.
 * doi: 10.1109/TCBB.2014.2311795
 *
 * @author fk
 */
public class CohesionMetric<LabelType extends Comparable<LabelType>> extends AbstractExtractionMetric<LabelType> implements ParallelizableMetric<LabelType>, DistributionMetric<LabelType> {

    public static final Comparator<Itemset<?>> COMPARATOR = Comparator.comparing(Itemset::getCohesion);

    private static final Logger logger = LoggerFactory.getLogger(CohesionMetric.class);

    private final double maximalCohesion;
    private final boolean vertexOne;
    private final int levelOfParallelism;
    private final ExecutorService executorService;
    private final Map<Itemset<LabelType>, Distribution> distributions;
    private Map<Itemset<LabelType>, Integer> itemsetObservationsCounts;

    public CohesionMetric(List<DataPoint<LabelType>> dataPoints, CohesionMetricConfiguration<LabelType> cohesionMetricConfiguration) {
        super(dataPoints, cohesionMetricConfiguration.getRepresentationSchemeType());
        distributions = new HashMap<>();
        maximalCohesion = cohesionMetricConfiguration.getMaximalCohesion();
        vertexOne = cohesionMetricConfiguration.isVertexOne();
        levelOfParallelism = cohesionMetricConfiguration.getLevelOfParallelism();
        executorService = (levelOfParallelism == -1) ? Executors.newWorkStealingPool() : Executors.newWorkStealingPool(levelOfParallelism);
    }

    @Override
    public String toString() {
        return "CohesionMetric{" +
               "maximalCohesion=" + maximalCohesion +
               '}';
    }

    @Override public int getMinimalItemsetSize() {
        return 2;
    }

    private Set<Itemset<LabelType>> calculateCohesion(Set<Itemset<LabelType>> itemsets) {

        // clear storage of extracted itemsets of previous round
        extractedItemsets = new HashMap<>();

        // the storage for observation counts
        itemsetObservationsCounts = new HashMap<>();

        // create chunks for parallel execution
        List<List<Itemset<LabelType>>> partitions = partition(new ArrayList<>(itemsets), (levelOfParallelism == -1) ? AVAILABLE_PROCESSORS : levelOfParallelism);

        // create jobs
        List<CohesionCalculator> jobs = partitions.stream()
                                                  .map(CohesionCalculator::new)
                                                  .collect(Collectors.toList());

        // execute jobs
        try {
            executorService.invokeAll(jobs).forEach(future -> {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new IllegalStateException(e);
                }
            });
        } catch (InterruptedException e) {
            logger.error("parallel cohesion execution of {} failed", this, e);
        }

        // normalize cohesion
        itemsets.forEach(itemset -> {
            if (itemsetObservationsCounts.containsKey(itemset)) {
                itemset.setCohesion(Math.sqrt(itemset.getCohesion() / itemsetObservationsCounts.get(itemset)));
            } else {
                // ignore itemsets that could not be found in the data
                logger.debug("no observations to determine cohesion for itemset {}, will be removed", itemset);
                itemset.setCohesion(Double.MAX_VALUE);
            }
        });

        // filter extracted itemsets
        filterExtractedItemsets();

        return itemsets;
    }

    @Override
    public Set<Itemset<LabelType>> filterItemsets(Set<Itemset<LabelType>> itemsets) {
        return calculateCohesion(itemsets).stream()
                                          .filter(itemset -> itemset.getCohesion() <= maximalCohesion)
                                          .collect(Collectors.toSet());
    }

    @Override
    protected void filterExtractedItemsets() {
        extractedItemsets.entrySet().removeIf(entry -> entry.getKey().getCohesion() > maximalCohesion);
    }

    private synchronized void incrementObservationCount(Itemset<LabelType> itemset) {
        if (itemsetObservationsCounts.containsKey(itemset)) {
            itemsetObservationsCounts.put(itemset, itemsetObservationsCounts.get(itemset) + 1);
        } else {
            itemsetObservationsCounts.put(itemset, 1);
        }
    }

    @Override
    public boolean isVertexOne() {
        return vertexOne;
    }

    @Override
    public Map<Itemset<LabelType>, Distribution> getDistributions() {
        return distributions;
    }

    private class CohesionCalculator implements Callable<Void> {
        private List<Itemset<LabelType>> itemsets;

        CohesionCalculator(List<Itemset<LabelType>> itemsets) {
            this.itemsets = itemsets;
        }

        @Override
        public Void call() throws Exception {

            for (Itemset<LabelType> itemset : itemsets) {

                for (DataPoint<LabelType> dataPoint : dataPoints) {

                    // generate candidates
                    VertexCandidateGenerator<LabelType> vertexCandidateGenerator = new VertexCandidateGenerator<>(itemset, dataPoint, obtainSquaredDistanceMatrix(dataPoint), vertexOne);
                    List<Itemset<LabelType>> candidates = vertexCandidateGenerator.generateCandidates();

                    if (!candidates.isEmpty()) {

                        // count observations of the itemset and sum up cohesion
                        incrementObservationCount(itemset);

                        // find and store candidate with minimal squared extent
                        Itemset<LabelType> bestCandidate = candidates.stream()
                                                                     .min((Comparator.comparingDouble(Itemsets::calculateMaximalSquaredExtent)))
                                                                     .orElseThrow(() -> new CohesionMetricException("failed to get candidate with minimal extent"));
                        addToExtractedItemsets(itemset, bestCandidate);

                        double squaredExtent = Itemsets.calculateMaximalSquaredExtent(bestCandidate);

                        // store extent for probability distribution
                        addObservationForItemset(itemset, Math.sqrt(squaredExtent));

                        itemset.setCohesion(itemset.getCohesion() + squaredExtent);
                    } else {
                        logger.debug("no candidates found for itemset {} in data point {}", itemset, dataPoint);
                    }
                }
            }

            return null;
        }
    }
}