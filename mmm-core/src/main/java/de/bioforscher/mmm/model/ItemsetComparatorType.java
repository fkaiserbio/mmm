package de.bioforscher.mmm.model;

import de.bioforscher.mmm.model.metrics.CohesionMetric;
import de.bioforscher.mmm.model.metrics.ConsensusMetric;
import de.bioforscher.mmm.model.metrics.SupportMetric;

import java.util.Comparator;

/**
 * @author fk
 */
public enum ItemsetComparatorType {
    SUPPORT(SupportMetric.COMPARATOR),
    COHESION(CohesionMetric.COMPARATOR),
    CONSENSUS(ConsensusMetric.COMPARATOR);

    public Comparator<Itemset<?>> getComparator() {
        return comparator;
    }

    private final Comparator<Itemset<?>> comparator;

    ItemsetComparatorType(Comparator<Itemset<?>> comparator) {
        this.comparator = comparator;
    }
}
