package de.bioforscher.mmm.model.metrics.cohesion;

/**
 * An exception if something goes wrong during the evaluation of {@link de.bioforscher.mmm.model.metrics.CohesionMetric}.
 *
 * @author fk
 */
public class CohesionMetricException extends RuntimeException {
    public CohesionMetricException() {
        super();
    }

    public CohesionMetricException(String message) {
        super(message);
    }

    public CohesionMetricException(String message, Throwable cause) {
        super(message, cause);
    }
}
