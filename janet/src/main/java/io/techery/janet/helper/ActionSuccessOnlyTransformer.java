package io.techery.janet.helper;

import io.techery.janet.ActionState;
import rx.Observable;
import rx.functions.Func1;

public class ActionSuccessOnlyTransformer<T> implements Observable.Transformer<ActionState<T>, T> {
    @Override public Observable<T> call(Observable<ActionState<T>> actionStateObservable) {
        return actionStateObservable
                .filter(new Func1<ActionState<T>, Boolean>() {
                    @Override public Boolean call(ActionState<T> tActionState) {
                        return tActionState.status == ActionState.Status.SUCCESS;
                    }
                })
                .map(new Func1<ActionState<T>, T>() {
                    @Override public T call(ActionState<T> tActionState) {
                        return tActionState.action;
                    }
                });
    }
}