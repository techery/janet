package io.techery.janet.helper;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.functions.Function;
import io.techery.janet.ActionState;

/**
 * To transform {@link ActionState} to action.
 * <pre>
 *     START - nothing
 *     PROGRESS - nothing
 *     SUCCESS - action with result
 *     FAIL - error. it's necessary to handle it using {@link Subscriber#onError(Throwable)}
 * </pre>
 */
public final class ActionStateToActionTransformer<A> implements FlowableTransformer<ActionState<A>, A> {

    @Override public Publisher<A> apply(Flowable<ActionState<A>> upstream) {
        return upstream.flatMap(new Function<ActionState<A>, Publisher<A>>() {
            @Override public Publisher<A> apply(ActionState<A> state) throws Exception {
                switch (state.status) {
                    case START:
                        return Flowable.empty();
                    case PROGRESS:
                        return Flowable.empty();
                    case SUCCESS:
                        return Flowable.just(state.action);
                    case FAIL:
                        return Flowable.error(new JanetActionException(state.exception, state.action));
                    default:
                        throw new IllegalArgumentException("Action status is unknown");
                }
            }
        });
    }
}
