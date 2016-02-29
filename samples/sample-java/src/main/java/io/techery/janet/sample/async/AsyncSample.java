package io.techery.janet.sample.async;


import com.google.gson.Gson;

import io.techery.janet.ActionPipe;
import io.techery.janet.AsyncActionService;
import io.techery.janet.Janet;
import io.techery.janet.async.actions.ConnectAsyncAction;
import io.techery.janet.gson.GsonConverter;
import io.techery.janet.helper.ActionStateSubscriber;
import io.techery.janet.nkzawa.SocketIO;
import rx.schedulers.Schedulers;

public class AsyncSample {

    public static void main(String... args) throws Exception {
        Janet janet = new Janet.Builder()
                .addService(new AsyncActionService("http://localhost:3000", new SocketIO(), new GsonConverter(new Gson())))
                .build();

        ActionPipe<TestAction> messagePipe = janet.createPipe(TestAction.class);

        TestAction action = new TestAction();
        action.body = new Body();
        action.body.id = 1;
        action.body.data = "test";

        janet.createPipe(ConnectAsyncAction.class, Schedulers.io())
                .createObservable(new ConnectAsyncAction())
                .subscribe(new ActionStateSubscriber<ConnectAsyncAction>()
                        .onSuccess(connectAsyncAction -> {
                            messagePipe.createObservable(action)
                                    .subscribe(new ActionStateSubscriber<TestAction>()
                                            .onSuccess(System.out::println)
                                            .onFail((testAction, throwable) -> throwable.printStackTrace()));

                            janet.createPipe(TestTwoAction.class)
                                    .observeResult()
                                    .subscribe(System.out::println);
                        }));


        while (true) {
            Thread.sleep(100);
        }

    }
}
