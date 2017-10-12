package de.bioforscher.mmm.model;

import de.bioforscher.mmm.model.metrics.*;

import java.util.Comparator;

/**
 * Comparators of {@link Itemset}s based on different {@link de.bioforscher.mmm.model.metrics.EvaluationMetric}s.
 *
 * @author fk
 */
public enum ItemsetComparatorType {
    SUPPORT(SupportMetric.COMPARATOR),
    COHESION(CohesionMetric.COMPARATOR),
    ADHERENCE(AdherenceMetric.COMPARATOR),
    CONSENSUS(ConsensusMetric.COMPARATOR),
    AFFINITY(AffinityMetric.COMPARATOR),
    SEPARATION(SeparationMetric.COMPARATOR);

    private final Comparator<Itemset<?>> comparator;

    ItemsetComparatorType(Comparator<Itemset<?>> comparator) {
        this.comparator = comparator;
    }

    public Comparator<Itemset<?>> getComparator() {
        return comparator;
    }
}
