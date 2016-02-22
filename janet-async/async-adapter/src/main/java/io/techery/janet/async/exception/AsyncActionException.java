package io.techery.janet.async.exception;

import io.techery.janet.JanetException;

public class AsyncActionException extends JanetException {

    public AsyncActionException(Throwable cause) {
        super(cause);
    }

    public AsyncActionException(String message, Throwable cause) {
        super(message, cause);
    }
}
