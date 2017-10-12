package de.bioforscher.mmm.model.metrics;

import de.bioforscher.mmm.model.DataPoint;
import de.bioforscher.mmm.model.Itemset;
import de.bioforscher.mmm.model.configurations.metrics.SupportMetricConfiguration;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A {@link SimpleEvaluationMetric} that evaluates {@link Itemset} candidates by their support (relative occurrence in the data).
 *
 * @author fk
 */
public class SupportMetric<LabelType extends Comparable<LabelType>> implements SimpleEvaluationMetric<LabelType> {

    public static final Comparator<Itemset<?>> COMPARATOR = Comparator.comparing((Function<Itemset<?>, Double>) Itemset::getSupport)
                                                                      .reversed();

    private final List<DataPoint<LabelType>> dataPoints;
    private final double minimalSupport;

    public SupportMetric(List<DataPoint<LabelType>> dataPoints, SupportMetricConfiguration<LabelType> supportMetricConfiguration) {
        this.dataPoints = dataPoints;
        this.minimalSupport = supportMetricConfiguration.getMinimalSupport();
    }

    @Override public String toString() {
        return "SupportMetric{" +
               "minimalSupport=" + minimalSupport +
               '}';
    }

    @Override
    public Set<Itemset<LabelType>> filterItemsets(Set<Itemset<LabelType>> itemsets) {
        return calculateSupport(itemsets).stream()
                                         .filter(itemset -> itemset.getSupport() >= minimalSupport)
                                         .collect(Collectors.toSet());
    }

    /**
     * Calculates the support for the given itemsets.
     *
     * @param itemsets The itemsets for which support should be calculated.
     * @return Itemsets with calculated support.
     */
    private Set<Itemset<LabelType>> calculateSupport(Set<Itemset<LabelType>> itemsets) {
        for (Itemset<LabelType> itemset : itemsets) {
            double support = 0.0;
            for (DataPoint<LabelType> dataPoint : dataPoints) {
                if (dataPoint.getItems().containsAll(itemset.getItems())) {
                    support++;
                }
            }
            // normalize support
            itemset.setSupport(support / dataPoints.size());
        }
        return itemsets;
    }
}
