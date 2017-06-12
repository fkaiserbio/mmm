package de.bioforscher.mmm.model.metrics;

import de.bioforscher.mmm.model.Itemset;

import java.util.Set;
import java.util.function.Predicate;

/**
 * @author fk
 */
public interface SimpleEvaluationMetric<LabelType extends Comparable<LabelType>> extends EvaluationMetric<LabelType> {

    Predicate<EvaluationMetric<?>> SIMPLE_METRIC_FILTER = evaluationMetric -> evaluationMetric instanceof SimpleEvaluationMetric;

    /**
     * Filters the given {@link Itemset}s according to the metric
     *
     * @param itemsets the itemsets to be filtered
     * @return the filtered itemsets
     */
    Set<Itemset<LabelType>> filterItemsets(Set<Itemset<LabelType>> itemsets);
}
