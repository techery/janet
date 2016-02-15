package io.techery.janet.sample;

import io.techery.janet.async.SyncPredicate;
import io.techery.janet.async.actions.BytesAsyncAction;
import io.techery.janet.async.annotations.AsyncAction;
import io.techery.janet.async.annotations.AsyncMessage;
import io.techery.janet.async.annotations.SyncedResponse;

//@AsyncAction(value = "test2")
public class AsyncTestBytesAction extends BytesAsyncAction{

    @SyncedResponse(TestSyncPredicate.class)
    AsyncTestBytesAction response;


    public static class TestSyncPredicate extends SyncPredicate {

        @Override public boolean isResponse(Object request, Object response) {
            return true;
        }
    }

    @Override public byte[] getBytes() {
        return new byte[0];
    }
}
