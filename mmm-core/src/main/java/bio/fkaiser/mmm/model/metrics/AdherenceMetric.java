package bio.fkaiser.mmm.model.metrics;

import bio.fkaiser.mmm.Itemsets;
import bio.fkaiser.mmm.model.DataPoint;
import bio.fkaiser.mmm.model.Distribution;
import bio.fkaiser.mmm.model.Itemset;
import bio.fkaiser.mmm.model.configurations.metrics.AdherenceMetricConfiguration;
import bio.fkaiser.mmm.model.metrics.cohesion.VertexCandidateGenerator;
import de.bioforscher.singa.mathematics.vectors.RegularVector;
import de.bioforscher.singa.mathematics.vectors.Vectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * An {@link ExtractionMetric} that extracts {@link Itemset}s based on the adherence of their items. For further reference see
 * <p/>
 * Kaiser, F. & Labudde, D. IEEE/ACM Trans. Comput. Biol. Bioinform. (under review)
 *
 * @author fk
 */
public class AdherenceMetric<LabelType extends Comparable<LabelType>> extends AbstractExtractionMetric<LabelType> implements ParallelizableMetric<LabelType>, DistributionMetric<LabelType> {

    public static final Comparator<Itemset<?>> COMPARATOR = Comparator.comparing(Itemset::getAdherence);
    private static final Logger logger = LoggerFactory.getLogger(AdherenceMetric.class);

    private final double maximalAdherence;
    private final double desiredSquaredExtent;
    private final double squaredExtentDelta;
    private final boolean vertexOne;
    private final int levelOfParallelism;
    private final ExecutorService executorService;
    private final Map<Itemset<LabelType>, Distribution> distributions;

    public AdherenceMetric(List<DataPoint<LabelType>> dataPoints, AdherenceMetricConfiguration<LabelType> adherenceMetricConfiguration) {
        super(dataPoints, adherenceMetricConfiguration.getRepresentationSchemeType());
        distributions = Collections.synchronizedMap(new HashMap<>());
        desiredSquaredExtent = adherenceMetricConfiguration.getDesiredExtent() * adherenceMetricConfiguration.getDesiredExtent();
        squaredExtentDelta = adherenceMetricConfiguration.getDesiredExtentDelta() * adherenceMetricConfiguration.getDesiredExtentDelta();
        maximalAdherence = adherenceMetricConfiguration.getMaximalAdherence();
        vertexOne = adherenceMetricConfiguration.isVertexOne();
        levelOfParallelism = adherenceMetricConfiguration.getLevelOfParallelism();
        executorService = (levelOfParallelism == -1) ? Executors.newWorkStealingPool() : Executors.newWorkStealingPool(levelOfParallelism);
    }

    @Override
    public boolean isVertexOne() {
        return vertexOne;
    }

    @Override public int getMinimalItemsetSize() {
        return 2;
    }

    @Override public String toString() {
        return "AdherenceMetric{" +
               "maximalAdherence=" + maximalAdherence +
               '}';
    }

    @Override public Set<Itemset<LabelType>> filterItemsets(Set<Itemset<LabelType>> itemsets) {
        return calculateAdherence(itemsets).stream()
                                           .filter(itemset -> itemset.getAdherence() <= maximalAdherence)
                                           .collect(Collectors.toSet());
    }

    private Set<Itemset<LabelType>> calculateAdherence(Set<Itemset<LabelType>> itemsets) {
        // clear storage of extracted itemsets of previous round
        extractedItemsets = new HashMap<>();

        // create chunks for parallel execution
        List<List<Itemset<LabelType>>> partitions = partition(new ArrayList<>(itemsets), (levelOfParallelism == -1) ? AVAILABLE_PROCESSORS : levelOfParallelism);

        // create jobs
        List<AdherenceCalculator> jobs = partitions.stream()
                                                   .map(AdherenceCalculator::new)
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
            logger.error("parallel adherence execution of {} failed", this, e);
        }

        for (Itemset<LabelType> itemset : itemsets) {
            if (distributions.containsKey(itemset)) {
                Distribution distribution = distributions.get(itemset);
                if (distribution.getObservations().size() < AdherenceMetricConfiguration.MINIMAL_OBSERVATIONS) {
                    logger.debug("not enough observations to determine adherence for itemset {}, will be removed", itemset);
                    itemset.setAdherence(Double.MAX_VALUE);
                    continue;
                }

                // calculate adherence of itemset (standard deviation of extent values)
                double[] elements = distribution.getObservations().stream()
                                                .mapToDouble(Double::doubleValue)
                                                .toArray();
                RegularVector vector = new RegularVector(elements);
                itemset.setAdherence(Vectors.getStandardDeviation(vector));

            } else {
                // ignore itemsets that could not be found in the data
                logger.debug("no observations to determine adherence for itemset {}, will be removed", itemset);
                itemset.setAdherence(Double.MAX_VALUE);
            }
        }

        // filter extracted itemsets
        filterExtractedItemsets();

        return itemsets;
    }

    @Override
    protected void filterExtractedItemsets() {
        extractedItemsets.entrySet().removeIf(entry -> entry.getKey().getAdherence() > maximalAdherence);
    }

    @Override
    public Map<Itemset<LabelType>, Distribution> getDistributions() {
        return distributions;
    }

    private class AdherenceCalculator implements Callable<Void> {
        private List<Itemset<LabelType>> itemsets;

        public AdherenceCalculator(List<Itemset<LabelType>> itemsets) {
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
                        // find and store candidates close to given adherence
                        for (Itemset<LabelType> candidate : candidates) {

                            // calculate the squared extent of the candidate
                            double candidateSquaredExtent = Itemsets.calculateMaximalSquaredExtent(candidate);

                            // if candidate extent fulfills constraints
                            if ((candidateSquaredExtent > (desiredSquaredExtent - squaredExtentDelta)) && candidateSquaredExtent < ((desiredSquaredExtent + squaredExtentDelta))) {
                                addToExtractedItemsets(itemset, candidate);
                                // store extent for probability distribution
                                addObservationForItemset(itemset, Math.sqrt(candidateSquaredExtent));
                            }
                        }
                    } else {
                        logger.debug("no candidates found for itemset {} in data point {}", itemset, dataPoint);
                    }
                }
            }

            return null;
        }
    }
}