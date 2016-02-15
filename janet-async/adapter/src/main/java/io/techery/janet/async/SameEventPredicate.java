package io.techery.janet.async;

import io.techery.janet.async.annotations.AsyncAction;

public class SameEventPredicate extends SyncPredicate {

    @Override public boolean isResponse(Object request, Object response) {
        AsyncAction a1 = request.getClass().getAnnotation(AsyncAction.class);
        if (a1 == null) {
            return false;
        }
        AsyncAction a2 = response.getClass().getAnnotation(AsyncAction.class);
        if (a2 == null) {
            return false;
        }
        return a1.value().equals(a2.value());
    }
}
