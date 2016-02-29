package io.techery.janet.async.exception;

import io.techery.janet.JanetException;

public class AsyncServiceException extends JanetException {

    public AsyncServiceException(Throwable cause) {
        super(cause);
    }

    public AsyncServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
