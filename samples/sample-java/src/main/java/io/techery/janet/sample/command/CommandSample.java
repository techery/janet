package io.techery.janet.sample.command;

import io.techery.janet.ActionPipe;
import io.techery.janet.CommandActionService;
import io.techery.janet.Janet;
import io.techery.janet.helper.ActionStateSubscriber;
import io.techery.janet.sample.command.actions.ThreadSleepAction;
import io.techery.janet.sample.command.actions.WrongAction;

public class CommandSample {

    public static void main(String... args) throws Throwable {
        Janet janet = new Janet.Builder()
                .addService(new CommandActionService())
                .build();

        // Send simple command, observe progress, try to cancel
        ActionPipe<ThreadSleepAction> actionPipe = janet.createPipe(ThreadSleepAction.class);

        actionPipe.observe().subscribe(new ActionStateSubscriber<ThreadSleepAction>()
                .onProgress((action, progress) -> {
                    System.out.println(progress);
                    if (progress >= 80) {
                        actionPipe.cancel(action);
                    }
                })
                .onSuccess(threadSleepAction -> System.out.println(threadSleepAction.getResult()))
                .onFail((threadSleepAction1, throwable) -> throwable.printStackTrace()));

        actionPipe.send(new ThreadSleepAction());

        // Try use action with broken contract
        try {
            janet.createPipe(WrongAction.class).send(new WrongAction());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
