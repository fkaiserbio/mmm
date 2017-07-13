package de.bioforscher.mmm;

/**
 * An exception that should be used when the mining fails.
 *
 * @author fk
 */
public class ItemsetMinerException extends RuntimeException {
    public ItemsetMinerException() {
    }

    public ItemsetMinerException(String message) {
        super(message);
    }

    public ItemsetMinerException(String message, Throwable cause) {
        super(message, cause);
    }
}
