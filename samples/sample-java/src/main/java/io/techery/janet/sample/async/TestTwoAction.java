package io.techery.janet.sample.async;

import io.techery.janet.async.annotations.AsyncAction;
import io.techery.janet.async.annotations.AsyncMessage;

@AsyncAction(value = "test2", incoming = true)
public class TestTwoAction {

    @AsyncMessage
    String data;

    @Override public String toString() {
        return "TestTwoAction{" +
                "data='" + data + '\'' +
                '}';
    }
}
