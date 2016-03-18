package io.techery.janet;

import rx.Observable;

public interface ReadActionPipe<A> extends Replays<A> {

    /**
     * Observe all states of specified action type
     */
    Observable<ActionState<A>> observe();

    /**
     * Observe success action only, if you want to catch any exceptions, use {@link ActionPipe#observe()}
     */
    Observable<A> observeSuccess();
}
