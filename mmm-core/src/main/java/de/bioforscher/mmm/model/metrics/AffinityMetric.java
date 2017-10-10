package de.bioforscher.mmm.model.metrics;

import de.bioforscher.mmm.model.Itemset;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author fk
 */
public class AffinityMetric<LabelType extends Comparable<LabelType>> implements ExtractionDependentMetric<LabelType> {
    @Override
    public Set<Itemset<LabelType>> filterItemsets(Set<Itemset<LabelType>> itemsets, Map<Itemset<LabelType>, List<Itemset<LabelType>>> extractedItemsets) {
        return null;
    }

    @Override
    public int getMinimalItemsetSize() {
        return 2;
    }

    @Override
    public void filterExtractedItemsets() {

    }
}
