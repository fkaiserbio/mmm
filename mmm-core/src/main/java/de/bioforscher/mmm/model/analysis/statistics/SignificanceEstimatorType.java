package de.bioforscher.mmm.model.analysis.statistics;

import de.bioforscher.mmm.model.metrics.CohesionMetric;
import de.bioforscher.mmm.model.metrics.ConsensusMetric;
import de.bioforscher.mmm.model.metrics.DistributionMetric;

/**
 * Defines types for estimation of significance of itemsets.
 *
 * @author fk
 */
public enum SignificanceEstimatorType {

    COHESION(CohesionMetric.class), CONSENSUS(ConsensusMetric.class);

    private Class<? extends DistributionMetric> distributionMetric;

    SignificanceEstimatorType(Class<? extends DistributionMetric> distributionMetric) {
        this.distributionMetric = distributionMetric;
    }

    public Class<? extends DistributionMetric> getDistributionMetric() {
        return distributionMetric;
    }
}
