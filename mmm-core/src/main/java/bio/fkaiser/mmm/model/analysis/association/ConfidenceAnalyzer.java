package bio.fkaiser.mmm.model.analysis.association;

import bio.fkaiser.mmm.ItemsetMiner;
import bio.fkaiser.mmm.model.Item;
import bio.fkaiser.mmm.model.Itemset;
import bio.fkaiser.mmm.model.analysis.AbstractItemsetMinerAnalyzer;
import bio.singa.core.utility.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Calculates and stores the confidence between {@link Itemset}s.
 * <b>NOTE: Confidence is not transitive.</b>
 *
 * @author fk
 */
public class ConfidenceAnalyzer<LabelType extends Comparable<LabelType>> extends AbstractItemsetMinerAnalyzer<LabelType> {

    private static final Logger logger = LoggerFactory.getLogger(ConfidenceAnalyzer.class);
    private TreeMap<Double, Pair<Itemset<LabelType>>> confidence;

    public ConfidenceAnalyzer(ItemsetMiner<LabelType> itemsetMiner) {
        super(itemsetMiner);

        confidence = new TreeMap<>(Collections.reverseOrder());
        calculateConfidence();
    }

    public TreeMap<Double, Pair<Itemset<LabelType>>> getConfidence() {
        return confidence;
    }

    private void calculateConfidence() {
        List<Itemset<LabelType>> totalItemsets = itemsetMiner.getTotalItemsets();
        for (int i = 0; i < itemsetMiner.getTotalItemsets().size(); i++) {
            Itemset<LabelType> itemsetOne = totalItemsets.get(i);
            for (int j = 0; j < totalItemsets.size(); j++) {
                Itemset<LabelType> itemsetTwo = totalItemsets.get(j);

                if (itemsetOne.equals(itemsetTwo) || (itemsetOne.getItems().size() != itemsetTwo.getItems().size())) {
                    continue;
                }

                // create joined items
                Set<Item<LabelType>> joinedItems = new TreeSet<>();
                joinedItems.addAll(itemsetOne.getItems());
                joinedItems.addAll(itemsetTwo.getItems());

                // check association between pairs
                itemsetMiner.getTotalItemsets().stream()
                            .filter(itemset -> itemset.getItems().equals(joinedItems))
                            .findFirst()
                            .ifPresent(joinedItemset -> storeConfidence(joinedItemset, itemsetOne, itemsetTwo));
            }
        }
    }

    private void storeConfidence(Itemset<LabelType> joinedItemset, Itemset<LabelType> itemsetOne, Itemset<LabelType> itemsetTwo) {
        double confidenceValue = joinedItemset.getSupport() / itemsetOne.getSupport();
        logger.debug("confidence for rule {} => {} is {}", itemsetOne.toSimpleString(), itemsetTwo.toSimpleString(), confidenceValue);
        confidence.put(confidenceValue, new Pair<>(itemsetOne, itemsetTwo));
    }
}
