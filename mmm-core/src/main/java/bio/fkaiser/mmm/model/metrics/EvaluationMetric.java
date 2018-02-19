package bio.fkaiser.mmm.model.metrics;

/**
 * A metric used to evaluate candidates during {@link bio.fkaiser.mmm.model.Itemset} mining. Each metric specifies a minimal {@link bio.fkaiser.mmm.model.Itemset} size to be applied.
 *
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
