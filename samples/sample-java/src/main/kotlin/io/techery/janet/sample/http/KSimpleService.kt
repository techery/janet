package io.techery.janet.sample.http

import com.google.gson.Gson
import io.techery.janet.HttpActionAdapter
import io.techery.janet.Janet
import io.techery.janet.gson.GsonConverter
import io.techery.janet.helper.ActionStateSubscriber
import io.techery.janet.okhttp.OkClient
import rx.Observable

const private val API_URL = "https://api.github.com"


fun main(args: Array<String>) {

    val janet = Janet.Builder()
            .addAdapter(HttpActionAdapter(API_URL, OkClient(), GsonConverter(Gson())))
            .build()

    val usersPipe = janet.createPipe(UsersAction::class.java)
    val userReposPipe = janet.createPipe(UserReposAction::class.java)

    usersPipe.observeResult()
            .filter({ it.isSuccess() })
            .subscribe({ println("received $it") }) { println(it) }

    usersPipe.createObservable(UsersAction())
            .subscribe();

    usersPipe.createObservable(UsersAction())
            .filter { it.action.isSuccess }
            .flatMap { Observable.from(it.action.response).first() }
            .flatMap { userReposPipe.createObservable(UserReposAction(it.getLogin())) }
            .subscribe(ActionStateSubscriber<UserReposAction>()
                    .onSuccess { println("repos request finished $it") }
                    .onFail { a, t -> println("repos request exception $t") }
            )


}


