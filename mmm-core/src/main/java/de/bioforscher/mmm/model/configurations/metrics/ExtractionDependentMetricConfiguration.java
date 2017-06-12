package de.bioforscher.mmm.model.configurations.metrics;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.bioforscher.mmm.model.metrics.EvaluationMetric;

import java.lang.reflect.InvocationTargetException;

/**
 * @author fk
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = SeparationMetricConfiguration.class),
               @JsonSubTypes.Type(value = ConsensusMetricConfiguration.class)})
public interface ExtractionDependentMetricConfiguration<LabelType extends Comparable<LabelType>> extends MetricConfiguration<LabelType> {
    @SuppressWarnings("unchecked")
    default EvaluationMetric<LabelType> createMetric() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return MetricConfigurationType.getMetric(this.getClass()).getConstructor(this.getClass()).newInstance(this);
    }
}
