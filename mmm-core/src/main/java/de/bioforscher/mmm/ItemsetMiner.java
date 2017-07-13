package de.bioforscher.mmm;

import de.bioforscher.mmm.model.DataPoint;
import de.bioforscher.mmm.model.Item;
import de.bioforscher.mmm.model.Itemset;
import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import de.bioforscher.mmm.model.metrics.*;
import de.bioforscher.singa.chemistry.algorithms.superimposition.consensus.ConsensusAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of the Aprioiri algorithm to generate frequent {@link Itemset}s. This uses {@link EvaluationMetric}s to evaluate candidates during generation.
 *
 * @author fk
 */
public class ItemsetMiner<LabelType extends Comparable<LabelType>> {

    private static final Logger logger = LoggerFactory.getLogger(ItemsetMiner.class);

    private final List<DataPoint<LabelType>> dataPoints;
    private final List<EvaluationMetric<LabelType>> evaluationMetrics;
    private final int maximalEpochs;
    private final Comparator<Itemset<?>> itemsetComparator;
    private Map<Itemset<LabelType>, ConsensusAlignment> totalClusteredItemsets;
    private Set<Itemset<LabelType>> previousCandidates;
    private Set<Itemset<LabelType>> removedPreviousCandidates;
    private Set<Itemset<LabelType>> candidates;
    private int previousItemsetSize;
    private List<Itemset<LabelType>> totalItemsets;
    private Map<Itemset<LabelType>, List<Itemset<LabelType>>> totalExtractedItemsets;

    public ItemsetMiner(List<DataPoint<LabelType>> dataPoints, List<EvaluationMetric<LabelType>> evaluationMetrics, ItemsetMinerConfiguration<LabelType> itemsetMinerConfiguration) {
        this.dataPoints = dataPoints;
        this.evaluationMetrics = evaluationMetrics;

        maximalEpochs = itemsetMinerConfiguration.getMaximalEpochs();
        itemsetComparator = itemsetMinerConfiguration.getItemsetComparatorType().getComparator();

        logger.info("initialized with {} data points", dataPoints.size());
        initialize();
    }

    public List<EvaluationMetric<LabelType>> getEvaluationMetrics() {
        return evaluationMetrics;
    }

    public List<Itemset<LabelType>> getTotalItemsets() {
        return totalItemsets;
    }

    public Map<Itemset<LabelType>, List<Itemset<LabelType>>> getTotalExtractedItemsets() {
        return totalExtractedItemsets;
    }

    public Map<Itemset<LabelType>, ConsensusAlignment> getTotalClusteredItemsets() {
        return totalClusteredItemsets;
    }

    private void initialize() {

        // initialize storage for all itemsets
        totalItemsets = new ArrayList<>();
        // initialize storage for extracted itemsets
        totalExtractedItemsets = new TreeMap<>(itemsetComparator);
        // initialize storage for clustered itemsets
        totalClusteredItemsets = new TreeMap<>(itemsetComparator);

        logger.info("creating initial 1-itemsets");
        previousCandidates = dataPoints.stream()
                                       .map(DataPoint::getItems)
                                       .flatMap(Collection::stream)
                                       .map(Itemset::of)
                                       // create shallow copies of the original itemsets (do not contain structure information)
                                       .map(Itemset::getCopy)
                                       .collect(Collectors.toSet());
        previousItemsetSize = 0;

        logger.info("1-itemsets are (size: {})\n\t{}", previousCandidates.size(), previousCandidates);
    }

    public void start() {
        logger.info("starting mining process");

        // initialize epoch counter
        int epochCounter = 1;
        // initialize storage for candidates
        candidates = new HashSet<>();
        // initialize storage for removed candidates per epoch
        removedPreviousCandidates = new HashSet<>();
        while (!candidates.isEmpty() || previousItemsetSize == 0) {

            // generate candidates of next round
            logger.info(">>>MINING EPOCH {}, generating new candidates<<<", epochCounter);
            generateCandidates();

            // evaluate metrics
            evaluateMetrics();

            // prune candidates of next round
            pruneCandidates();

            // notify the user if maximal number of epochs was reached and stop mining
            if (epochCounter == maximalEpochs && maximalEpochs != -1) {
                logger.info(">>>MINING TERMINATED: EPOCH LIMIT REACHED<<<");
                break;
            }

            // notify the user if mining converged
            if (candidates.isEmpty() && previousItemsetSize != 0) {
                logger.info(">>>MINING TERMINATED: CONVERGED<<<");
            }
            previousCandidates = candidates;

            // update epoch counter
            epochCounter++;
        }

        // sort results
        totalItemsets.sort(itemsetComparator);
    }

    private void pruneCandidates() {
        // immediately return if first iteration
        if (previousItemsetSize == 0) {
            return;
        }

        // remove all itemsets of new candidates that are containing previously removed ones
        candidates.removeIf(candidate -> removedPreviousCandidates.stream()
                                                                  .anyMatch(removedPreviousCandidate -> candidate.getItems().containsAll(removedPreviousCandidate.getItems())));

        // also terminate if previous candidates are empty (evaluation metrics filtered all potential new candidates)
        if (previousCandidates.isEmpty()) {
            candidates.clear();
        }

        // clear storage of removed previous candidates
        removedPreviousCandidates.clear();

        logger.info("pruned new candidates are (size: {})\n\t{}", candidates.size(), candidates);
    }

    private void evaluateMetrics() {

        Predicate<EvaluationMetric<LabelType>> minimalItemsetSizeFilter = evaluationMetric -> evaluationMetric.getMinimalItemsetSize() <= previousItemsetSize;

        // evaluate standard metrics
        evaluationMetrics.stream()
                         .filter(SimpleEvaluationMetric.SIMPLE_METRIC_FILTER)
                         .filter(minimalItemsetSizeFilter)
                         .map(evaluationMetric -> (SimpleEvaluationMetric<LabelType>) evaluationMetric)
                         .forEach(evaluationMetric -> {
                             logger.info("evaluating simple evaluation metric {}", evaluationMetric);
                             Set<Itemset<LabelType>> filteredCandidates = evaluationMetric.filterItemsets(previousCandidates);
                             removeFromPreviousCandidates(evaluationMetric, filteredCandidates);
                         });

        // evaluate extraction metrics
        Map<Itemset<LabelType>, List<Itemset<LabelType>>> extractedItemsets = new HashMap<>();
        evaluationMetrics.stream()
                         .filter(ExtractionMetric.EXTRACTION_METRIC_FILTER)
                         .filter(minimalItemsetSizeFilter)
                         .map(evaluationMetric -> (ExtractionMetric<LabelType>) evaluationMetric)
                         .forEach(extractionMetric -> {
                             logger.info("evaluating extraction metric {}", extractionMetric);
                             Set<Itemset<LabelType>> filteredCandidates = extractionMetric.filterItemsets(previousCandidates);
                             removeFromPreviousCandidates(extractionMetric, filteredCandidates);
                             extractedItemsets.putAll(extractionMetric.getExtractedItemsets());
                         });

        // evaluate extraction-dependent metrics
        Map<Itemset<LabelType>, ConsensusAlignment> clusteredItemsets = new HashMap<>();
        evaluationMetrics.stream()
                         .filter(ExtractionDependentMetric.EXTRACTION_DEPENDENT_METRIC_FILTER)
                         .filter(minimalItemsetSizeFilter)
                         .map(evaluationMetric -> (ExtractionDependentMetric<LabelType>) evaluationMetric)
                         .forEach(extractionDependentMetric -> {
                             logger.info("evaluating extraction-dependent metric {}", extractionDependentMetric);
                             Set<Itemset<LabelType>> filteredCandidates = extractionDependentMetric.filterItemsets(previousCandidates, extractedItemsets);
                             removeFromPreviousCandidates(extractionDependentMetric, filteredCandidates);
                             extractionDependentMetric.filterExtractedItemsets();
                             // store clustered itemsets if consensus metric is used
                             if (extractionDependentMetric instanceof ConsensusMetric) {
                                 clusteredItemsets.putAll(((ConsensusMetric<LabelType>) extractionDependentMetric).getClusteredItemsets());
                             }
                         });

        // synchronize clustered and extracted itemsets
        clusteredItemsets.keySet().removeIf(itemset -> !extractedItemsets.containsKey(itemset));

        if (previousItemsetSize > 1) {
            // globally store itemsets which passed all metrics
            totalItemsets.addAll(previousCandidates);
            // globally store extracted itemsets
            totalExtractedItemsets.putAll(extractedItemsets);
            // globally store clustered itemsets
            totalClusteredItemsets.putAll(clusteredItemsets);
        }

        logger.info("pruned previous candidates after evaluation of all metrics are (size: {})\n\t{}", previousCandidates.size(), previousCandidates);
    }

    private void removeFromPreviousCandidates(EvaluationMetric<LabelType> evaluationMetric, Set<Itemset<LabelType>> filteredCandidates) {
        Set<Itemset<LabelType>> currentRemovedPreviousCandidates = previousCandidates.stream()
                                                                                     .filter(previousCandidate -> !filteredCandidates.contains(previousCandidate))
                                                                                     .collect(Collectors.toSet());
        // removed candidates have to add up per epoch
        if (removedPreviousCandidates.isEmpty()) {
            removedPreviousCandidates = currentRemovedPreviousCandidates;
        } else {
            removedPreviousCandidates.addAll(currentRemovedPreviousCandidates);
        }

        logger.info("removing previous candidates by {} (size: {})\n\t{}", evaluationMetric, removedPreviousCandidates.size(), removedPreviousCandidates);
        previousCandidates.removeAll(removedPreviousCandidates);
        logger.info("pruned previous candidates by {} (size: {})\n\t{}", evaluationMetric, previousCandidates.size(), previousCandidates);
    }

    private void generateCandidates() {
        // determine size of previous candidates
        previousItemsetSize = previousCandidates.stream()
                                                .map(itemset -> itemset.getItems().size())
                                                .distinct()
                                                .findFirst()
                                                .orElseThrow(() -> new ItemsetMinerException("failed during candidate generation"));

        // create new candidates
        candidates = new HashSet<>();
        for (Itemset<LabelType> itemsetOne : previousCandidates) {
            boolean passedItemsetTwo = false;
            for (Itemset<LabelType> itemsetTwo : previousCandidates) {
                if (passedItemsetTwo) {
                    // concatenate previous items
                    Set<Item<LabelType>> items = Stream.concat(itemsetOne.getItems().stream(), itemsetTwo.getItems().stream())
                                                       .collect(Collectors.toSet());
                    // ignore combinations of repetitive sets
                    if (items.size() != previousItemsetSize + 1) {
                        continue;
                    }
                    // create and store new itemset
                    Itemset<LabelType> newItemset = Itemset.of(items);
                    candidates.add(newItemset);
                } else {
                    if (itemsetOne.equals(itemsetTwo)) {
                        passedItemsetTwo = true;
                    }
                }
            }
        }
        logger.info("new candidates are (size: {})\n\t{}", candidates.size(), candidates);
    }
}
