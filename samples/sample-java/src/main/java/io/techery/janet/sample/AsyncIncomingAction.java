package io.techery.janet.sample;

import io.techery.janet.async.annotations.AsyncAction;
import io.techery.janet.async.annotations.AsyncMessage;

@AsyncAction(value = "test2", incoming = true)
public class AsyncIncomingAction {

    @AsyncMessage
    String data;
}
