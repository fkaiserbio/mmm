package de.bioforscher.mmm.model.configurations.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.bioforscher.mmm.model.configurations.Jsonizable;

/**
 * The {@link Jsonizable} configuration of the {@link de.bioforscher.mmm.model.metrics.SupportMetric}.
 *
 * @author fk
 */
@JsonTypeName("SUPPORT")
public class SupportMetricConfiguration<LabelType extends Comparable<LabelType>> implements SimpleMetricConfiguration<LabelType>, Jsonizable<SupportMetricConfiguration> {

    /**
     * the default value for the required minimal support
     */
    public static final double DEFAULT_MINIMAL_SUPPORT = 0.8;

    @JsonProperty("minimal-support")
    private double minimalSupport = DEFAULT_MINIMAL_SUPPORT;

    public double getMinimalSupport() {
        return minimalSupport;
    }

    public void setMinimalSupport(double minimalSupport) {
        this.minimalSupport = minimalSupport;
    }

    @Override public String toString() {
        return "SupportMetricConfiguration{" +
               "minimalSupport=" + minimalSupport +
               '}';
    }
}
