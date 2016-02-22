package io.techery.janet;

public class JanetException extends RuntimeException {

    public JanetException(String message, Throwable cause) {
        super(message, cause);
    }

    public JanetException(Throwable cause) {
        super(cause);
    }
}
