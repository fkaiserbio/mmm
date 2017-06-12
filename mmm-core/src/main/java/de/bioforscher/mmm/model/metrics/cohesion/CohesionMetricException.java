package de.bioforscher.mmm.model.metrics.cohesion;

/**
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
