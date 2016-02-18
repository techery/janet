package io.techery.janet;

import io.techery.janet.async.SyncPredicate;

public final class SimpleSyncPredicate implements SyncPredicate {
    @Override public boolean isResponse(Object requestAction, Object response) {
        return true;
    }
}
