package io.techery.janet.util;

import java.util.concurrent.Executor;

public class FakeExecutor implements Executor {
    @Override public void execute(Runnable command) {
        command.run();
    }
}
