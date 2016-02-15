package io.techery.janet.async;

public class SyncPredicate<T, R> {

    public SyncPredicate() {}

    public boolean isResponse(T request, R response) {
        return true;
    }
}
