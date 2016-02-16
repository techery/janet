package io.techery.janet.async;

public abstract class SyncPredicate {

    public boolean isResponse(Object requestAction, Object response) {
        return true;
    }
}
