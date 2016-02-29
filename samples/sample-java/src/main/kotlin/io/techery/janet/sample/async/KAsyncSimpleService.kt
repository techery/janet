package io.techery.janet.sample.async

import com.google.gson.Gson
import io.techery.janet.AsyncActionService
import io.techery.janet.Janet
import io.techery.janet.async.actions.ConnectAsyncAction
import io.techery.janet.gson.GsonConverter
import io.techery.janet.helper.ActionStateSubscriber
import io.techery.janet.nkzawa.SocketIO
import rx.functions.Action1

const private val API_URL = "http://localhost:3000"


fun main(args: Array<String>) {

    val janet = Janet
            .Builder()
            .addService(AsyncActionService(API_URL, SocketIO(), GsonConverter(Gson())))
            .build()

    val messagePipe = janet.createPipe(TestAction::class.java)

    var action = TestAction()
    action.body = Body()
    action.body.id = 1
    action.body.data = "test"

    janet.createPipe(ConnectAsyncAction::class.java)
            .createObservable(ConnectAsyncAction())
            .subscribe(ActionStateSubscriber<ConnectAsyncAction>()
                    .onSuccess {
                        messagePipe.createObservable(action)
                                .subscribe(ActionStateSubscriber<TestAction>()
                                        .onSuccess({ println(it) }))

                        janet.createPipe(TestTwoAction::class.java).observeResult().subscribe(Action1<TestTwoAction> { println(it) })
                    })


    while (true) {
        Thread.sleep(100)
    }


}


