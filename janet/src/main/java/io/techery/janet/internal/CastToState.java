package io.techery.janet.internal;

import io.techery.janet.ActionState;
import rx.Observable;

/**
 * Internal class for casting something to {@link ActionState}
 */
public class CastToState<A> implements Observable.Transformer<ActionState, ActionState<A>> {

    private final Class<ActionState<A>> type;

    @SuppressWarnings("unchecked") public CastToState() {
        type = (Class<ActionState<A>>) new TypeToken<ActionState<A>>() {}.getRawType();
    }

    @Override public Observable<ActionState<A>> call(Observable<ActionState> source) {
        return source.cast(type);
    }
}
