package io.techery.janet;


import io.reactivex.Flowable;
import io.reactivex.flowables.ConnectableFlowable;

interface Replays<A> {
    /**
     * Observe all states of specified action type with cache.
     * Last action state will be emitted immediately after subscribe.
     *
     * @see ConnectableFlowable#replay(int)
     */
    Flowable<ActionState<A>> observeWithReplay();


    /**
     * Observe action result with cache.
     * Emmit the latest result, if exist, immediately after subscribe.
     *
     * @see ConnectableFlowable#replay(int)
     * @see ActionPipe#observeSuccess()
     */
    Flowable<A> observeSuccessWithReplay();

    /**
     * Clear cached action
     */
    void clearReplays();
}
