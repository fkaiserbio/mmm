package bio.fkaiser.mmm.model.analysis.statistics;

import bio.fkaiser.mmm.ItemsetMiner;
import bio.fkaiser.mmm.Itemsets;
import bio.fkaiser.mmm.model.*;
import bio.fkaiser.mmm.model.configurations.metrics.ConsensusMetricConfiguration;
import bio.fkaiser.mmm.model.metrics.*;
import bio.fkaiser.mmm.model.metrics.cohesion.VertexCandidateGenerator;
import de.bioforscher.singa.structure.algorithms.superimposition.affinity.AffinityAlignment;
import de.bioforscher.singa.structure.algorithms.superimposition.consensus.ConsensusAlignment;
import de.bioforscher.singa.structure.algorithms.superimposition.consensus.ConsensusBuilder;
import de.bioforscher.singa.structure.model.oak.StructuralEntityFilter.AtomFilter;
import de.bioforscher.singa.structure.model.oak.StructuralMotif;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * This allows the sampling of background distributions of {@link Itemset}s regarding supported {@link DistributionMetric}s.
 * To sample the background distributions the labels of each {@link DataPoint} are shuffled such that the original frequency is not changed.
 *
 * @author fk
 */
class DistributionSampler<LabelType extends Comparable<LabelType>> extends DataPointCache<LabelType> {

    private static final Logger logger = LoggerFactory.getLogger(DistributionSampler.class);
    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    private final List<DataPoint<LabelType>> dataPoints;
    private final List<Itemset<LabelType>> itemsets;
    private final int sampleSize;
    private final int levelOfParallelism;
    private final Class<? extends DistributionMetric> distributionMetricType;
    private final Class<? extends ExtractionMetric> extractionMetricType;
    private final Map<Itemset<LabelType>, Distribution> backgroundDistributions;
    private final boolean vertexOne;
    private final ExecutorService executorService;

    private double clusterCutoff;
    private Map<Itemset<LabelType>, Integer> itemsetObservationCounts;

    DistributionSampler(ItemsetMiner<LabelType> itemsetMiner, Class<? extends DistributionMetric> distributionMetricType, int levelOfParallelism, int sampleSize) {

        super(itemsetMiner.getEvaluationMetrics().stream()
                          .filter(ExtractionMetric.class::isInstance)
                          .map(ExtractionMetric.class::cast)
                          .map(ExtractionMetric::getRepresentationSchemeType)
                          .filter(Objects::nonNull)
                          .findAny()
                          .orElse(null));

        this.distributionMetricType = distributionMetricType;
        this.levelOfParallelism = levelOfParallelism;
        this.sampleSize = sampleSize;

        // work with copy of data points such that original labels stay the same
        dataPoints = itemsetMiner.getDataPoints().stream()
                                 .map(DataPoint::getCopy)
                                 .collect(Collectors.toList());
        itemsets = itemsetMiner.getTotalItemsets();
        backgroundDistributions = new HashMap<>();
        executorService = (levelOfParallelism == -1) ? Executors.newWorkStealingPool() : Executors.newWorkStealingPool(levelOfParallelism);

        logger.info("distribution sampler initialized for distribution metric type " + distributionMetricType.getSimpleName());

        // get associated extraction metric and store parameters
        ExtractionMetric extractionMetric = itemsetMiner.getEvaluationMetrics().stream()
                                                        .filter(ExtractionMetric.class::isInstance)
                                                        .map(ExtractionMetric.class::cast)
                                                        .findAny()
                                                        .orElseThrow(() -> new DistributionSamplerException("failed to determine used extraction metric"));
        extractionMetricType = extractionMetric.getClass();
        vertexOne = extractionMetric.isVertexOne();

        if (distributionMetricType == ConsensusMetric.class) {
            clusterCutoff = itemsetMiner.getItemsetMinerConfiguration().getExtractionDependentMetricConfigurations().stream()
                                        .filter(ConsensusMetricConfiguration.class::isInstance)
                                        .map(ConsensusMetricConfiguration.class::cast)
                                        .findAny()
                                        .orElseThrow(() -> new DistributionSamplerException("failed to determine extraction metric type"))
                                        .getClusterCutoffValue();
        }

        runBackgroundSampling();
    }

    public Map<Itemset<LabelType>, Distribution> getBackgroundDistributions() {
        return backgroundDistributions;
    }

    private void addSampleValueForItemset(Itemset<LabelType> itemset, double sampleValue) {
        if (backgroundDistributions.containsKey(itemset)) {
            backgroundDistributions.get(itemset).addObservationValue(sampleValue);
        } else {
            Distribution distribution = new Distribution(distributionMetricType);
            distribution.addObservationValue(sampleValue);
            backgroundDistributions.put(itemset, distribution);
        }
    }

    /**
     * Runs the background sampling for the specified number of samples.
     */
    private void runBackgroundSampling() {
        for (int i = 0; i < sampleSize; i++) {
            if (i % Math.floor(sampleSize / 10) == 0) {
                logger.info("running background sampling round {} of {}", i + 1, sampleSize);
            }
            randomizeDataPoints();
            // reset itemset observation counts in each sample round
            itemsetObservationCounts = new HashMap<>();

            // create chunks for parallel execution
            List<List<Itemset<LabelType>>> partitions = partition(new ArrayList<>(itemsets), (levelOfParallelism == -1) ? AVAILABLE_PROCESSORS : levelOfParallelism);

            // create jobs
            List<DistributionCalculator> jobs = partitions.stream()
                                                          .map(DistributionCalculator::new)
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
        }
    }

    /**
     * This assigns the labels of the {@link Item}s for each {@link DataPoint} randomly.
     */
    private void randomizeDataPoints() {
        logger.debug("randomizing data points");
        for (DataPoint<LabelType> dataPoint : dataPoints) {
            logger.trace("shuffling data point {}", dataPoint);
            List<LabelType> shuffledItems = dataPoint.getItems().stream()
                                                     .map(Item::getLabel)
                                                     .collect(Collectors.toList());
            Collections.shuffle(shuffledItems);
            ListIterator<LabelType> itemListIterator = shuffledItems.listIterator();
            for (Item<LabelType> item : dataPoint.getItems()) {
                LabelType newLabel = itemListIterator.next();
                item.setLabel(newLabel);
                itemListIterator.remove();
            }
        }
    }

    /**
     * Increments the observation count of the given Itemset.
     *
     * @param itemset The {@link Itemset} for which the count should be incremented.
     */
    private synchronized void incrementObservationCount(Itemset<LabelType> itemset) {
        if (itemsetObservationCounts.containsKey(itemset)) {
            itemsetObservationCounts.put(itemset, itemsetObservationCounts.get(itemset) + 1);
        } else {
            itemsetObservationCounts.put(itemset, 1);
        }
    }

    private List<List<Itemset<LabelType>>> partition(List<Itemset<LabelType>> itemsets, int k) {

        List<List<Itemset<LabelType>>> partitions = new ArrayList<>();
        // init partitions
        for (int i = 0; i < k; i++) {
            partitions.add(new ArrayList<>(itemsets.size() / k + 1));
        }

        // distribute objects fairly among all partitions
        for (int i = 0; i < itemsets.size(); i++) {
            partitions.get(i % k).add(itemsets.get(i));
        }

        return partitions;
    }

    private class DistributionCalculator implements Callable<Void> {

        private List<Itemset<LabelType>> itemsets;

        private DistributionCalculator(List<Itemset<LabelType>> itemsets) {
            this.itemsets = itemsets;
        }

        @Override
        public Void call() {

            for (Itemset<LabelType> itemset : itemsets) {

                // create shallow copy background itemset
                Itemset<LabelType> backgroundItemset = new Itemset<>(itemset.getItems());
                List<Itemset<LabelType>> allCandidates = new ArrayList<>();
                for (DataPoint<LabelType> dataPoint : dataPoints) {
                    // create candidates for current itemset
                    VertexCandidateGenerator<LabelType> candidateGenerator = new VertexCandidateGenerator<>(backgroundItemset, dataPoint, obtainSquaredDistanceMatrix(dataPoint), vertexOne);
                    List<Itemset<LabelType>> candidates = candidateGenerator.generateCandidates();
                    if (!candidates.isEmpty()) {
                        if (extractionMetricType == CohesionMetric.class) {
                            // find and store candidate with minimal squared extent
                            Itemset<LabelType> bestCandidate = candidates.stream()
                                                                         .min((Comparator.comparingDouble(Itemsets::calculateMaximalSquaredExtent)))
                                                                         .orElseThrow(() -> new DistributionSamplerException("failed to get candidate with minimal extent"));
                            // calculate squared extent of best candidate
                            double bestCandidateSquaredExtent = Itemsets.calculateMaximalSquaredExtent(bestCandidate);

                            if (distributionMetricType == CohesionMetric.class) {
                                // store extent for background probability distribution
                                backgroundItemset.setCohesion(backgroundItemset.getCohesion() + bestCandidateSquaredExtent);
                                incrementObservationCount(itemset);
                            }
                            allCandidates.add(bestCandidate);
                        }
                        // TODO implement support for adherence metric here
                    }
                }
                // calculate consensus if wanted
                if (distributionMetricType == ConsensusMetric.class) {
                    if (!allCandidates.isEmpty()) {
                        List<StructuralMotif> structuralMotifs = allCandidates.stream()
                                                                              .map(Itemset::getStructuralMotif)
                                                                              .filter(Optional::isPresent)
                                                                              .map(Optional::get)
                                                                              .collect(Collectors.toList());
                        // perform consensus alignment with backbone atoms only
                        ConsensusAlignment consensusAlignment = ConsensusBuilder.create()
                                                                                .inputStructuralMotifs(structuralMotifs)
                                                                                .atomFilter(AtomFilter.isBackbone())
                                                                                .clusterCutoff(clusterCutoff)
                                                                                .alignWithinClusters(false)
                                                                                .idealSuperimposition(false)
                                                                                .run();
                        backgroundItemset.setConsensus(consensusAlignment.getNormalizedConsensusScore());
                        addSampleValueForItemset(itemset, backgroundItemset.getConsensus());
                    }
                } else if (distributionMetricType == AffinityMetric.class) {
                    if (!allCandidates.isEmpty()) {
                        List<StructuralMotif> structuralMotifs = allCandidates.stream()
                                                                              .map(Itemset::getStructuralMotif)
                                                                              .filter(Optional::isPresent)
                                                                              .map(Optional::get)
                                                                              .collect(Collectors.toList());
                        // perform affinity alignment with backbone atoms only
                        AffinityAlignment affinityAlignment = AffinityAlignment.create()
                                                                               .inputStructuralMotifs(structuralMotifs)
                                                                               .atomFilter(AtomFilter.isBackbone())
                                                                               .alignWithinClusters(false)
                                                                               .idealSuperimposition(false)
                                                                               .run();
                        backgroundItemset.setAffinity(AffinityMetric.calculateAffinity(affinityAlignment));
                        addSampleValueForItemset(itemset, backgroundItemset.getAffinity());
                    }
                }
                // normalize and store cohesion value
                if (distributionMetricType == CohesionMetric.class) {
                    // normalize cohesion value
                    backgroundItemset.setCohesion(Math.sqrt(backgroundItemset.getCohesion() / itemsetObservationCounts.get(backgroundItemset)));
                    addSampleValueForItemset(itemset, backgroundItemset.getCohesion());
                }
            }
            return null;
        }
    }
}