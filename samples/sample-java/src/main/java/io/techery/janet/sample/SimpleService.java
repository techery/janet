package io.techery.janet.sample;

import com.google.gson.Gson;

import io.techery.janet.ActionStateSubscriber;
import io.techery.janet.HttpActionAdapter;
import io.techery.janet.Janet;
import io.techery.janet.JanetPipe;
import io.techery.janet.gson.GsonConverter;
import io.techery.janet.okhttp.OkClient;
import rx.Observable;

public class SimpleService {

    private static final String API_URL = "https://api.github.com";

    public static void main(String... args) {
        Janet janet = new Janet.Builder()
                .addAdapter(new HttpActionAdapter(API_URL, new OkClient(), new GsonConverter(new Gson())))
                .addInterceptor(System.out::println)
                .build();

        JanetPipe<UsersAction> usersExecutor = janet.createExecutor(UsersAction.class);
        JanetPipe<UserReposAction> userReposExecutor = janet.createExecutor(UserReposAction.class);

        usersExecutor.observeActions()
                .filter(BaseAction::isSuccess)
                .subscribe(
                        action -> System.out.println("received " + action),
                        System.err::println
                );

        usersExecutor.createObservable(new UsersAction())
                .filter(state -> state.action.isSuccess())
                .flatMap(state -> Observable.<User>from(state.action.response).first())
                .flatMap(user -> userReposExecutor.createObservable(new UserReposAction(user.getLogin())))
                .subscribe(new ActionStateSubscriber<UserReposAction>()
                        .onSuccess(action -> System.out.println("repos request finished " + action))
                        .onFail(throwable -> System.err.println("repos request throwable " + throwable))
                        .onServerError(action -> System.err.println("repos request http throwable " + action)));

    }
}
