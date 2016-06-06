package io.techery.janet.helper;

import io.techery.janet.JanetException;

/**
 * JanetException with an action inside
 */
public class JanetActionException extends JanetException {

    private final Object action;

    JanetActionException(Throwable cause, Object action) {
        super(cause);
        this.action = action;
    }

    public Object getAction() {
        return action;
    }
}
