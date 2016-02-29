package io.techery.janet.helper;

import io.techery.janet.ActionState;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * To transform {@link ActionState} to action.
 * <pre>
 *     START - nothing
 *     PROGRESS - nothing
 *     SUCCESS - action with result
 *     FAIL - error. it's necessary to handle it using {@link Subscriber#onError(Throwable)}
 * </pre>
 */
public final class ActionStateToActionTransformer<A> implements Observable.Transformer<ActionState<A>, A> {

    @Override
    public Observable<A> call(Observable<ActionState<A>> observable) {
        return observable.flatMap(new Func1<ActionState<A>, Observable<A>>() {
            @Override
            public Observable<A> call(ActionState<A> state) {
                switch (state.status) {
                    case START:
                        return Observable.never();
                    case PROGRESS:
                        return Observable.never();
                    case SUCCESS:
                        return Observable.just(state.action);
                    case FAIL:
                        return Observable.error(state.exception);
                    default:
                        throw new IllegalArgumentException("Action status is unknown");
                }
            }
        });
    }
}