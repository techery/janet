package io.techery.janet;

/**
 * Superclass for all errors and exceptions in Janet
 */
public class JanetException extends Throwable {

    JanetException() {}

    public JanetException(String message, Throwable cause) {
        super(message, cause);
    }

    public JanetException(Throwable cause) {
        super(cause);
    }
}
