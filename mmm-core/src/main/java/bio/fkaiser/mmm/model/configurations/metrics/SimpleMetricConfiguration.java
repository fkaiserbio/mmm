package bio.fkaiser.mmm.model.configurations.metrics;

import bio.fkaiser.mmm.model.DataPoint;
import bio.fkaiser.mmm.model.metrics.EvaluationMetric;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * An interface implemented by configurations for {@link bio.fkaiser.mmm.model.metrics.SeparationMetric}s.
 *
 * @author fk
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = SupportMetricConfiguration.class)})
public interface SimpleMetricConfiguration<LabelType extends Comparable<LabelType>> extends MetricConfiguration<LabelType> {
    @SuppressWarnings("unchecked")
    default EvaluationMetric<LabelType> createMetric(List<DataPoint<LabelType>> dataPoints) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return MetricConfigurationType.getMetric(this.getClass()).getConstructor(List.class, this.getClass()).newInstance(dataPoints, this);
    }
}
