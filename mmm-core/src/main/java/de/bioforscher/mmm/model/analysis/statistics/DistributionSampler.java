package de.bioforscher.mmm.model.analysis.statistics;

import de.bioforscher.mmm.ItemsetMiner;
import de.bioforscher.mmm.Itemsets;
import de.bioforscher.mmm.model.*;
import de.bioforscher.mmm.model.configurations.metrics.ConsensusMetricConfiguration;
import de.bioforscher.mmm.model.metrics.*;
import de.bioforscher.mmm.model.metrics.cohesion.CohesionMetricException;
import de.bioforscher.mmm.model.metrics.cohesion.VertexCandidateGenerator;
import de.bioforscher.singa.chemistry.algorithms.superimposition.consensus.ConsensusAlignment;
import de.bioforscher.singa.chemistry.algorithms.superimposition.consensus.ConsensusBuilder;
import de.bioforscher.singa.chemistry.physical.branches.StructuralMotif;
import de.bioforscher.singa.chemistry.physical.model.StructuralEntityFilter.AtomFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fk
 */
public class DistributionSampler<LabelType extends Comparable<LabelType>> extends DataPointCache<LabelType> {

    private static final Logger logger = LoggerFactory.getLogger(DistributionSampler.class);
    private static final int SAMPLE_SIZE = 10;

    private final Class<? extends ExtractionMetric> extractionMetricType;
    private final Class<? extends DistributionMetric> distributionMetricType;
    private final List<DataPoint<LabelType>> dataPoints;
    private final boolean vertexOne;
    private Map<Itemset<LabelType>, Integer> itemsetObservationsCounts;


    private double clusterCutoff;
    private Map<Itemset<LabelType>, Distribution> originalDistributions;
    private Map<Itemset<LabelType>, Distribution> backgroundDistributions;

    public DistributionSampler(ItemsetMiner<LabelType> itemsetMiner, Class<? extends DistributionMetric> distributionMetricType) throws IOException, URISyntaxException {

        super(itemsetMiner.getEvaluationMetrics().stream()
                          .filter(ExtractionMetric.class::isInstance)
                          .map(ExtractionMetric.class::cast)
                          .map(ExtractionMetric::getRepresentationSchemeType)
                          .filter(Objects::nonNull)
                          .findAny()
                          .orElse(null));

        logger.info("distribution sampler initialized for distribution metric type " + distributionMetricType.getSimpleName());
        // store distribution metrics of original run
        for (EvaluationMetric<LabelType> evaluationMetric : itemsetMiner.getEvaluationMetrics()) {
            if (distributionMetricType.isInstance(evaluationMetric)) {
                originalDistributions = ((DistributionMetric<LabelType>) evaluationMetric).getDistributions();
            }
        }
        if (originalDistributions == null) {
            throw new DistributionSamplerException("failed to obtain original distributions for type " + distributionMetricType);
        }

        extractionMetricType = itemsetMiner.getEvaluationMetrics().stream()
                                           .filter(ExtractionMetric.class::isInstance)
                                           .map(ExtractionMetric.class::cast)
                                           .map(ExtractionMetric::getClass)
                                           .findAny()
                                           .orElseThrow(() -> new DistributionSamplerException("failed to determine extraction metric type"));
        vertexOne = itemsetMiner.getEvaluationMetrics().stream()
                                .filter(ExtractionMetric.class::isInstance)
                                .map(ExtractionMetric.class::cast)
                                .map(ExtractionMetric::isVertexOne)
                                .findFirst()
                                .orElseThrow(() -> new DistributionSamplerException("failed to determine mode of Vertex algorithm used"));

        this.dataPoints = itemsetMiner.getDataPoints();
        this.distributionMetricType = distributionMetricType;

        if (distributionMetricType == ConsensusMetric.class) {
            clusterCutoff = itemsetMiner.getItemsetMinerConfiguration().getExtractionDependentMetricConfigurations().stream()
                                        .filter(ConsensusMetricConfiguration.class::isInstance)
                                        .map(ConsensusMetricConfiguration.class::cast)
                                        .findAny()
                                        .orElseThrow(() -> new IllegalArgumentException("failed to determine extraction metric type"))
                                        .getClusterCutoffValue();
        }

        backgroundDistributions = new HashMap<>();
        itemsetObservationsCounts = new HashMap<>();

        runBackgroundSampling();
    }

    public Map<Itemset<LabelType>, Distribution> getBackgroundDistributions() {
        return backgroundDistributions;
    }

    private void addObservationForItemset(Itemset<LabelType> itemset, double observationValue) {
        if (backgroundDistributions.containsKey(itemset)) {
            backgroundDistributions.get(itemset).addObservationValue(observationValue);
        } else {
            Distribution distribution = new Distribution(distributionMetricType);
            distribution.addObservationValue(observationValue);
            backgroundDistributions.put(itemset, distribution);
        }
    }

    private void runBackgroundSampling() throws IOException, URISyntaxException {

        for (int i = 0; i < SAMPLE_SIZE; i++) {
            logger.info("running background sampling round {}", i + 1);
            randomizeDataPoints();

            for (Itemset<LabelType> itemset : originalDistributions.keySet()) {
                // create background itemset
                Itemset<LabelType> backgroundItemset = itemset.getCopy();
                List<Itemset<LabelType>> allCandidates = new ArrayList<>();
                for (DataPoint<LabelType> dataPoint : dataPoints) {
                    VertexCandidateGenerator<LabelType> candidateGenerator = new VertexCandidateGenerator<>(itemset, dataPoint, obtainSquaredDistanceMatrix(dataPoint), vertexOne);
                    List<Itemset<LabelType>> candidates = candidateGenerator.generateCandidates();
                    if (!candidates.isEmpty()) {
                        if (extractionMetricType == CohesionMetric.class) {
                            // find and store candidate with minimal squared extent
                            Itemset<LabelType> bestCandidate = candidates.stream()
                                                                         .min((Comparator.comparingDouble(Itemsets::calculateMaximalSquaredExtent)))
                                                                         .orElseThrow(() -> new CohesionMetricException("failed to get candidate with minimal extent"));

                            // calculate squared extent of best candidate
                            double bestCandidateSquaredExtent = Itemsets.calculateMaximalSquaredExtent(bestCandidate);

                            if (distributionMetricType == CohesionMetric.class) {
                                // store extent for background probability distribution
//                                addObservationForItemset(itemset, Math.sqrt(bestCandidateSquaredExtent));
                                itemset.setCohesion(itemset.getCohesion() + bestCandidateSquaredExtent);
                                incrementObservationCount(itemset);
                            }
                            allCandidates.add(bestCandidate);
                        }
                        if (extractionMetricType == AdherenceMetric.class) {
                            // find and store candidates
                            for (Itemset<LabelType> candidate : candidates) {

                                // calculate the squared extent of the candidate
                                double candidateSquaredExtent = Itemsets.calculateMaximalSquaredExtent(candidate);

                                if (distributionMetricType == AdherenceMetric.class) {
                                    // store extent for background probability distribution
                                    addObservationForItemset(itemset, Math.sqrt(candidateSquaredExtent));
                                    allCandidates.add(candidate);
                                }
                            }
                        }
                    }
                }

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
                        consensusAlignment.getAlignmentTrace().forEach(observationValue -> addObservationForItemset(itemset, observationValue));
                    }
                }
                // normalize cohesion value
                itemset.setCohesion(Math.sqrt(itemset.getCohesion() / itemsetObservationsCounts.get(itemset)));
                addObservationForItemset(backgroundItemset, itemset.getCohesion());
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

    public Map<Itemset<LabelType>, Distribution> getOriginalDistributions() {
        return originalDistributions;
    }

    private synchronized void incrementObservationCount(Itemset<LabelType> itemset) {
        if (itemsetObservationsCounts.containsKey(itemset)) {
            itemsetObservationsCounts.put(itemset, itemsetObservationsCounts.get(itemset) + 1);
        } else {
            itemsetObservationsCounts.put(itemset, 1);
        }
    }
}