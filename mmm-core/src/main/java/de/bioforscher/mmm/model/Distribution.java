package de.bioforscher.mmm.model;

import de.bioforscher.mmm.model.metrics.DistributionMetric;

import java.util.ArrayList;
import java.util.List;

/**
 * An object that stores values observed by {@link DistributionMetric}s during mining.
 *
 * @author fk
 */
public class Distribution {

    private Class<? extends DistributionMetric> distributionMetricType;
    private List<Double> observations;

    public Distribution(Class<? extends DistributionMetric> distributionMetricType) {
        this.distributionMetricType = distributionMetricType;
        observations = new ArrayList<>();
    }

    public List<Double> getObservations() {
        return observations;
    }

    public Class<? extends DistributionMetric> getDistributionMetricType() {
        return distributionMetricType;
    }

    @Override
    public String toString() {
        return "Distribution{" +
               "distributionMetricType=" + distributionMetricType.getSimpleName() +
               ", observations=" + observations.size() +
               '}';
    }

    public void addObservationValue(double observationValue) {
        observations.add(observationValue);
    }
}
