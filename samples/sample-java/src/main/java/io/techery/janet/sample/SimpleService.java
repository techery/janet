package io.techery.janet.sample;

import com.google.gson.Gson;

import io.techery.janet.ActionPipe;
import io.techery.janet.ActionState;
import io.techery.janet.HttpActionAdapter;
import io.techery.janet.Janet;
import io.techery.janet.gson.GsonConverter;
import io.techery.janet.helper.ActionStateSubscriber;
import io.techery.janet.okhttp.OkClient;
import rx.Observable;

public class SimpleService {

    private static final String API_URL = "https://api.github.com";

    public static void main(String... args) {
        Janet janet = new Janet.Builder()
                .addAdapter(new HttpActionAdapter(API_URL, new OkClient(), new GsonConverter(new Gson())))
                .addInterceptor(ActionState.Status.START, state -> {
                    System.out.println("intercepted start " + state);
                    if (state.action instanceof UsersAction) {
                        ((UsersAction) state.action).since = 1;
                    }
                })
                .addInterceptor(ActionState.Status.SUCCESS, state -> System.out.println("intercepted success " + state))
                .addInterceptor(ActionState.Status.SERVER_ERROR, state -> System.out.println("intercepted server_error " + state))
                .addInterceptor(ActionState.Status.FAIL, state -> System.out.println("intercepted fail " + state))
                .build();

        ActionPipe<UsersAction> usersPipe = janet.createPipe(UsersAction.class);
        ActionPipe<UserReposAction> userReposPipe = janet.createPipe(UserReposAction.class);

        usersPipe.observeActions()
                .filter(BaseAction::isSuccess)
                .subscribe(
                        action -> System.out.println("received " + action),
                        System.err::println
                );

        usersPipe.createObservable(new UsersAction())
                .filter(state -> state.action.isSuccess())
                .flatMap(state -> Observable.<User>from(state.action.response).first())
                .flatMap(user -> userReposPipe.createObservable(new UserReposAction(user.getLogin())))
                .subscribe(new ActionStateSubscriber<UserReposAction>()
                        .onSuccess(action -> System.out.println("repos request finished " + action))
                        .onFail(throwable -> System.err.println("repos request throwable " + throwable))
                        .onServerError(action -> System.err.println("repos request http throwable " + action)));
    }
}
