package io.techery.janet.async;

public interface SyncPredicate<T, R> {

    boolean isResponse(T request, R response);
}
