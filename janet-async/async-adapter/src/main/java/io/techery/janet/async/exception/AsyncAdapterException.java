package io.techery.janet.async.exception;

import io.techery.janet.JanetException;

public class AsyncAdapterException extends JanetException {

    public AsyncAdapterException(Throwable cause) {
        super(cause);
    }

    public AsyncAdapterException(String message, Throwable cause) {
        super(message, cause);
    }
}
