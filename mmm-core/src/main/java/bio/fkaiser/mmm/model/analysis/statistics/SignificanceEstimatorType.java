package bio.fkaiser.mmm.model.analysis.statistics;

import bio.fkaiser.mmm.model.metrics.AffinityMetric;
import bio.fkaiser.mmm.model.metrics.CohesionMetric;
import bio.fkaiser.mmm.model.metrics.ConsensusMetric;
import bio.fkaiser.mmm.model.metrics.DistributionMetric;

/**
 * Defines types for estimation of significance of itemsets.
 *
 * @author fk
 */
public enum SignificanceEstimatorType {

    COHESION(CohesionMetric.class), CONSENSUS(ConsensusMetric.class), AFFINITY(AffinityMetric.class);

    private Class<? extends DistributionMetric> distributionMetric;

    SignificanceEstimatorType(Class<? extends DistributionMetric> distributionMetric) {
        this.distributionMetric = distributionMetric;
    }

    public Class<? extends DistributionMetric> getDistributionMetric() {
        return distributionMetric;
    }
}
