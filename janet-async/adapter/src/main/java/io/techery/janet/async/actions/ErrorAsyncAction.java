package io.techery.janet.async.actions;

import io.techery.janet.async.annotations.AsyncAction;

@AsyncAction(value = "error", incoming = true)
final public class ErrorAsyncAction implements SystemAction{

    private final Throwable error;

    public ErrorAsyncAction(Throwable error) {this.error = error;}

    public Throwable getError() {
        return error;
    }
}
