package io.techery.janet.sample.async;

import io.techery.janet.async.annotations.AsyncAction;
import io.techery.janet.async.annotations.AsyncMessage;
import io.techery.janet.async.annotations.SyncedResponse;

@AsyncAction(value = "test", incoming = true)
public class TestAction {

    @AsyncMessage
    Body body;

    @SyncedResponse(SyncPredicate.class)
    TestAction response;

    @Override public String toString() {
        return "TestAction{" +
                "body=" + body +
                ", response=" + response +
                '}';
    }

    public static class SyncPredicate extends io.techery.janet.async.SyncPredicate {

        @Override public boolean isResponse(Object requestAction, Object response) {
            return ((TestAction) requestAction).body.id == ((TestAction) response).body.id;
        }
    }
}
