package de.bioforscher.mmm.model.metrics;

import de.bioforscher.mmm.model.Itemset;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author fk
 */
public interface ExtractionDependentMetric<LabelType extends Comparable<LabelType>> extends EvaluationMetric<LabelType> {

    Predicate<EvaluationMetric<?>> EXTRACTION_DEPENDENT_METRIC_FILTER = evaluationMetric -> evaluationMetric instanceof ExtractionDependentMetric;

    Set<Itemset<LabelType>> filterItemsets(Set<Itemset<LabelType>> itemsets, Map<Itemset<LabelType>, List<Itemset<LabelType>>> extractedItemsets);

    void filterExtractedItemsets();
}
