package bio.fkaiser.mmm.model.configurations.metrics;

import bio.fkaiser.mmm.model.metrics.EvaluationMetric;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.lang.reflect.InvocationTargetException;

/**
 * An interface implemented by configurations for {@link bio.fkaiser.mmm.model.metrics.ExtractionDependentMetric}s.
 *
 * @author fk
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = SeparationMetricConfiguration.class),
               @JsonSubTypes.Type(value = ConsensusMetricConfiguration.class),
               @JsonSubTypes.Type(value = AffinityMetricConfiguration.class)})
public interface ExtractionDependentMetricConfiguration<LabelType extends Comparable<LabelType>> extends MetricConfiguration<LabelType> {
    @SuppressWarnings("unchecked")
    default EvaluationMetric<LabelType> createMetric() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return MetricConfigurationType.getMetric(this.getClass()).getConstructor(this.getClass()).newInstance(this);
    }
}
