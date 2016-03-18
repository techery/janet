package io.techery.janet;

import rx.Observable;

interface Replays<A> {
    /**
     * Observe all states of specified action type with cache.
     * Last action state will be emitted immediately after subscribe.
     *
     * @see Observable#replay(int)
     */
    Observable<ActionState<A>> observeWithReplay();


    /**
     * Observe action result with cache.
     * Emmit the latest result, if exist, immediately after subscribe.
     *
     * @see Observable#replay(int)
     * @see ActionPipe#observeSuccess()
     */
    Observable<A> observeSuccessWithReplay();

    /**
     * Clear cached action
     */
    void clearReplays();
}
