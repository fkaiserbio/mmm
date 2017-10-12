package de.bioforscher.mmm.model.configurations.metrics;

import de.bioforscher.mmm.model.metrics.*;

import java.util.stream.Stream;

/**
 * An {@link Enum} to connect {@link MetricConfiguration}s with their associated metric via reflection.
 *
 * @author fk
 */

public enum MetricConfigurationType {

    SUPPORT_CONFIGURATION(SupportMetricConfiguration.class, SupportMetric.class),
    COHESION_CONFIGURATION(CohesionMetricConfiguration.class, CohesionMetric.class),
    ADHERENCE_CONFIGURATION(AdherenceMetricConfiguration.class, AdherenceMetric.class),
    CONSENSUS_CONFIGURATION(ConsensusMetricConfiguration.class, ConsensusMetric.class),
    AFFINITY_CONFIGURATION(AffinityMetricConfiguration.class, AffinityMetric.class),
    SEPARATION_CONFIGURATION(SeparationMetricConfiguration.class, SeparationMetric.class);

    private Class<? extends MetricConfiguration> metricConfiguration;
    private Class<? extends EvaluationMetric> associatedMetric;

    MetricConfigurationType(Class<? extends MetricConfiguration> metricConfiguration, Class<? extends EvaluationMetric> associatedMetric) {
        this.metricConfiguration = metricConfiguration;
        this.associatedMetric = associatedMetric;
    }

    /**
     * Returns the metric for a given {@link MetricConfiguration} class.
     *
     * @param metricConfiguration The {@link MetricConfiguration} class for which a corresponding {@link EvaluationMetric} class should be retrieved.
     * @return
     */
    public static Class<? extends EvaluationMetric> getMetric(Class<? extends MetricConfiguration> metricConfiguration) {
        return Stream.of(values())
                     .filter(metricConfigurationType -> metricConfigurationType.getMetricConfiguration().equals(metricConfiguration))
                     .findFirst()
                     .map(MetricConfigurationType::getAssociatedMetric)
                     .orElseThrow(() -> new IllegalArgumentException("no compatible metric for this configuration found"));
    }

    public Class<? extends MetricConfiguration> getMetricConfiguration() {
        return metricConfiguration;
    }

    public Class<? extends EvaluationMetric> getAssociatedMetric() {
        return associatedMetric;
    }
}
