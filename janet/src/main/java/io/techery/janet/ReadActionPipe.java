package io.techery.janet;

import rx.Observable;
import rx.functions.Func1;

public interface ReadActionPipe<A> extends Replays<A> {

    /** Observe all states of specified action type */
    Observable<ActionState<A>> observe();

    /**
     * Observe actions with success status only.
     * <p>Use {@link #observe()} to track other statuses and exceptions.
     */
    Observable<A> observeSuccess();

    /** Returns a presentation of the {@link ReadActionPipe} with applied predicate */
    ReadActionPipe<A> filter(Func1<? super A, Boolean> predicate);
}
