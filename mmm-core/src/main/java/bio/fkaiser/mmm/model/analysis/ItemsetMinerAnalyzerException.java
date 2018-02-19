package bio.fkaiser.mmm.model.analysis;

/**
 * An exception that should be used when developing {@link ItemsetMinerAnalyzer} modules.
 *
 * @author fk
 */
public class ItemsetMinerAnalyzerException extends RuntimeException {
    public ItemsetMinerAnalyzerException() {
    }

    public ItemsetMinerAnalyzerException(String message) {
        super(message);
    }

    public ItemsetMinerAnalyzerException(String message, Throwable cause) {
        super(message, cause);
    }
}
