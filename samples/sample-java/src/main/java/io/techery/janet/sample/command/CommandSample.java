package io.techery.janet.sample.command;

import java.util.concurrent.TimeUnit;

import io.techery.janet.ActionPipe;
import io.techery.janet.CommandActionService;
import io.techery.janet.Janet;
import io.techery.janet.helper.ActionStateSubscriber;
import io.techery.janet.sample.command.actions.ThreadSleepAction;
import io.techery.janet.sample.command.actions.WrongAction;
import rx.Observable;

public class CommandSample {

    public static void main(String... args) throws Throwable {
        Janet janet = new Janet.Builder()
                .addService(new CommandActionService())
                .build();

        ActionPipe<ThreadSleepAction> actionPipe = janet.createPipe(ThreadSleepAction.class);

        actionPipe.observe()
                .subscribe(new ActionStateSubscriber<ThreadSleepAction>()
                        .onProgress((action, progress) -> System.out.println(progress)));

        ThreadSleepAction action = new ThreadSleepAction();

        Observable.fromCallable(() -> null)
                .delay(ThreadSleepAction.DURATION - 1000, TimeUnit.MILLISECONDS)
                .subscribe(o -> actionPipe.cancel(action));

        actionPipe.send(action);

        janet.createPipe(WrongAction.class).send(new WrongAction());
    }
}
