package de.bioforscher.mmm.model.metrics;

/**
 * @author fk
 */
public interface EvaluationMetric<LabelType extends Comparable<LabelType>> {

    int DEFAULT_MINIMAL_ITEMSET_SIZE = 0;

    /**
     * Get the minimal epoch for the respective evaluation metric.
     *
     * @return Minimal epoch upon which the metric is considered.
     */
    default int getMinimalItemsetSize() {
        return DEFAULT_MINIMAL_ITEMSET_SIZE;
    }

}
