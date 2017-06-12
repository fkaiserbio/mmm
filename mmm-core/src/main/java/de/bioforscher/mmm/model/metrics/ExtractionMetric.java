package de.bioforscher.mmm.model.metrics;

import de.bioforscher.mmm.model.Itemset;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author fk
 */
public interface ExtractionMetric<LabelType extends Comparable<LabelType>> extends EvaluationMetric<LabelType> {

    Predicate<EvaluationMetric<?>> EXTRACTION_METRIC_FILTER = evaluationMetric -> evaluationMetric instanceof ExtractionMetric;

    Map<Itemset<LabelType>, List<Itemset<LabelType>>> getExtractedItemsets();

    /**
     * Filters the given {@link Itemset}s according to the metric
     *
     * @param itemsets the itemsets to be filtered
     * @return the filtered itemsets
     */
    Set<Itemset<LabelType>> filterItemsets(Set<Itemset<LabelType>> itemsets);
}
