package io.techery.janet.sample.command.actions;

import io.techery.janet.CommandActionBase;
import io.techery.janet.command.annotations.CommandAction;
import rx.Observable;

@CommandAction
public class ThreadSleepAction extends CommandActionBase {

    public final static long DURATION = 10 * 1000;

    private boolean cancel;

    @Override protected Observable<?> run(CommandCallback callback) throws Throwable {
        int seconds = 0;
        while (!cancel) {
            Thread.sleep(1000);
            callback.onProgress((int) ((++seconds * 100) / (DURATION / 1000)));
        }
        return null;
    }

    @Override public void cancel() {
        cancel = true;
    }
}
