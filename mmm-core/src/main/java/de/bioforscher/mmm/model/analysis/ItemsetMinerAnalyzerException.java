package de.bioforscher.mmm.model.analysis;

/**
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
