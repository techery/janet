package io.techery.janet.sample.async;

import io.techery.janet.async.SyncPredicate;
import io.techery.janet.async.annotations.AsyncAction;
import io.techery.janet.async.annotations.AsyncMessage;
import io.techery.janet.async.annotations.SyncedResponse;

@AsyncAction(value = "test", incoming = true)
public class TestAction {

    @AsyncMessage
    Body body;

    @SyncedResponse(value = TestSyncPredicate.class, timeout = 3000)
    TestAction response;

    @Override public String toString() {
        return "TestAction{" +
                "body=" + body +
                ", response=" + response +
                '}';
    }

    public static class TestSyncPredicate implements SyncPredicate<TestAction, TestAction> {

        @Override public boolean isResponse(TestAction requestAction, TestAction response) {
            return requestAction.body.id == response.body.id;
        }
    }
}
