package io.techery.janet.sample;

import io.techery.janet.async.SyncPredicate;
import io.techery.janet.async.annotations.AsyncAction;
import io.techery.janet.async.annotations.AsyncMessage;
import io.techery.janet.async.annotations.SyncedResponse;

@AsyncAction(value = "test", incoming = true)
public class AsyncTestAction {

    @AsyncMessage
    String data;

    @SyncedResponse(TestSyncPredicate.class)
    AsyncIncomingAction response;


    public static class TestSyncPredicate extends SyncPredicate {

        @Override public boolean isResponse(Object request, Object response) {
            return true;
        }
    }

}
