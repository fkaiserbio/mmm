package bio.fkaiser.mmm.model.analysis.statistics;

/**
 * @author fk
 */
public class DistributionSamplerException extends RuntimeException {

    public DistributionSamplerException() {
    }

    public DistributionSamplerException(String message) {
        super(message);
    }

    public DistributionSamplerException(String message, Throwable cause) {
        super(message, cause);
    }
}