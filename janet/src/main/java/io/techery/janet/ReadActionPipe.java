package io.techery.janet;

import rx.Observable;

public interface ReadActionPipe<A> {

    /**
     * Observe all states of specified action type
     */
    Observable<ActionState<A>> observe();

    /**
     * Observe all states of specified action type with cache.
     * Last action state will be emitted immediately after subscribe.
     *
     * @see Observable#replay(int)
     */
    Observable<ActionState<A>> observeWithReplay();

    /**
     * Observe success action only, if you want to catch any exceptions, use {@link ActionPipe#observe()}
     */
    Observable<A> observeSuccess();

    /**
     * Observe action result with cache.
     * Emmit the latest result, if exist, immediately after subscribe.
     *
     * @see Observable#replay(int)
     * @see ActionPipe#observeSuccess()
     */
    Observable<A> observeSuccessWithReplay();
}
