package de.bioforscher.mmm;

/**
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
