package io.techery.janet;

import rx.functions.Action;

public interface Interceptor {
    void intercept(Action action);
}
