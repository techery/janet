package io.techery.janet.sample;

import java.io.IOException;

import io.techery.janet.async.SyncPredicate;
import io.techery.janet.async.annotations.AsyncAction;
import io.techery.janet.async.annotations.AsyncMessage;
import io.techery.janet.async.annotations.SyncedResponse;
import io.techery.janet.body.ActionBody;

@AsyncAction(value = "test", incoming = true)
public class AsyncTestAction {

    @AsyncMessage
    TestBytesBody data;

    @SyncedResponse(TestSyncPredicate.class)
    AsyncIncomingAction response;

    public static class TestSyncPredicate extends SyncPredicate {

        @Override public boolean isResponse(Object request, Object response) {
            return true;
        }
    }

    public static class TestBytesBody extends ActionBody {

        @Override public byte[] getContent() throws IOException {
            return new byte[0];
        }

        public TestBytesBody(String mimeType) {
            super(mimeType);
        }
    }

}
