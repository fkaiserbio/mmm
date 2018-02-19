package bio.fkaiser.mmm.model.metrics;

import bio.fkaiser.mmm.model.Itemset;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link EvaluationMetric} that supports multi-processor calculation.
 *
 * @author fk
 */
public interface ParallelizableMetric<LabelType extends Comparable<LabelType>> extends EvaluationMetric<LabelType> {

    int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    default List<List<Itemset<LabelType>>> partition(List<Itemset<LabelType>> itemsets, int k) {

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
}
