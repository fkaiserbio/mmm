package bio.fkaiser.mmm.model.metrics;

import bio.fkaiser.mmm.model.Distribution;
import bio.fkaiser.mmm.model.Itemset;

import java.util.Map;

/**
 * An {@link EvaluationMetric} that allows sampling of a distribution for each {@link Itemset}, e.g. cohesion.
 *
 * @author fk
 */
public interface DistributionMetric<LabelType extends Comparable<LabelType>> extends EvaluationMetric<LabelType> {

    Map<Itemset<LabelType>, Distribution> getDistributions();

    default void addObservationForItemset(Itemset<LabelType> itemset, double observationValue) {
        Map<Itemset<LabelType>, Distribution> distributions = getDistributions();
        if (distributions.containsKey(itemset)) {
            distributions.get(itemset).addObservationValue(observationValue);
        } else {
            Distribution distribution = new Distribution(this.getClass());
            distribution.addObservationValue(observationValue);
            distributions.put(itemset, distribution);
        }
    }
}
