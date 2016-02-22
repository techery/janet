package io.techery.janet.http.exception;

import io.techery.janet.JanetException;

public class HttpAdapterException extends JanetException {

    public HttpAdapterException(Throwable cause) {
        super(cause);
    }

    public HttpAdapterException(String message, Throwable cause) {
        super(message, cause);
    }
}
