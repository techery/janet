package io.techery.janet.sample.command.actions;

import io.techery.janet.CommandActionBase;
import io.techery.janet.command.annotations.CommandAction;

@CommandAction
public class ThreadSleepAction extends CommandActionBase<String> {

    public final static long DURATION = 10L * 1000L;

    @Override protected void run(CommandCallback<String> callback) {
        new Thread(() -> {
            int seconds = 0;
            while (!isCanceled()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    callback.onFail(e);
                }
                callback.onProgress((++seconds * 100) / ((int) (DURATION / 1000L)));
            }
            callback.onSuccess("FINISHED");
        }).start();
    }

}
