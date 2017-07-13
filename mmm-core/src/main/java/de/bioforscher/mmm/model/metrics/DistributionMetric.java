package de.bioforscher.mmm.model.metrics;

import de.bioforscher.mmm.model.Distribution;
import de.bioforscher.mmm.model.Itemset;

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
