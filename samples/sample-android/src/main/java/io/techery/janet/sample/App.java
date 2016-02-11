package io.techery.janet.sample;

import android.app.Application;
import android.content.Context;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;

import io.techery.janet.ActionPipe;
import io.techery.janet.ActionState;
import io.techery.janet.HttpActionAdapter;
import io.techery.janet.Janet;
import io.techery.janet.gson.GsonConverter;
import io.techery.janet.okhttp.OkClient;
import io.techery.janet.sample.network.UserReposAction;
import io.techery.janet.sample.network.UsersAction;
import io.techery.janet.sample.tools.AndroidLogHook;
import rx.plugins.RxJavaPlugins;
import rx.schedulers.Schedulers;

public class App extends Application {

    private static final String API_URL = "https://api.github.com";

    private Janet gitHubAPI;
    private ActionPipe<UsersAction> usersPipe;
    private ActionPipe<UserReposAction> userReposPipe;

    @Override
    public void onCreate() {
        super.onCreate();
        RxJavaPlugins.getInstance().registerObservableExecutionHook(new AndroidLogHook());
    }

    public Janet getGitHubAPI() {
        if (gitHubAPI == null) {
            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.networkInterceptors().add(chain -> chain.proceed(chain.request().newBuilder()
                    .addHeader("test", "test")
                    .build()));
            gitHubAPI = new Janet.Builder()
                    .addAdapter(new HttpActionAdapter(API_URL, new OkClient(okHttpClient), new GsonConverter(new Gson())))
                    .addInterceptor(ActionState.Status.SUCCESS, state -> {
                        // DO SMTH
                    })
                    .build();
        }
        return gitHubAPI;
    }

    public ActionPipe<UsersAction> getUsersPipe() {
        if (usersPipe == null) {
            usersPipe = getGitHubAPI().createPipe(UsersAction.class)
                    .pimp(Schedulers.io());
        }
        return usersPipe;
    }

    public ActionPipe<UserReposAction> getUserReposPipe() {
        if (userReposPipe == null) {
            userReposPipe = getGitHubAPI().createPipe(UserReposAction.class)
                    .pimp(Schedulers.io());
        }
        return userReposPipe;
    }

    public static App get(Context context) {
        return (App) context.getApplicationContext();
    }
}
