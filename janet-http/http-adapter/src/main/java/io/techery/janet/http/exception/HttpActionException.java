package io.techery.janet.http.exception;

import io.techery.janet.JanetException;

public class HttpActionException extends JanetException {

    public HttpActionException(Throwable cause) {
        super(cause);
    }

    public HttpActionException(String message, Throwable cause) {
        super(message, cause);
    }
}
