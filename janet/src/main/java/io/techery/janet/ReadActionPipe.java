package io.techery.janet;


import io.reactivex.Flowable;
import io.reactivex.functions.Predicate;

public interface ReadActionPipe<A> extends Replays<A> {

    /** Observe all states of specified action type */
    Flowable<ActionState<A>> observe();

    /**
     * Observe actions with success status only.
     * <p>Use {@link #observe()} to track other statuses and exceptions.
     */
    Flowable<A> observeSuccess();

    /** Returns a presentation of the {@link ReadActionPipe} with applied predicate */
    ReadActionPipe<A> filter(Predicate<? super A> predicate);
}
