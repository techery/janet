package io.techery.janet.http.exception;

import io.techery.janet.JanetException;

public class HttpServiceException extends JanetException {

    public HttpServiceException(Throwable cause) {
        super(cause);
    }

    public HttpServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
