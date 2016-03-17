package io.techery.janet;

import rx.Observable;

/**
 * Read only type of {@linkplain ActionPipe}
 */
public final class ReadOnlyActionPipe<A> implements ReadActionPipe<A> {

    private final ReadActionPipe<A> actionPipe;

    public ReadOnlyActionPipe(ReadActionPipe<A> actionPipe) {
        this.actionPipe = actionPipe;
    }

    /**
     * {@inheritDoc}
     */
    @Override public Observable<ActionState<A>> observe() {
        return actionPipe.observe();
    }

    /**
     * {@inheritDoc}
     */
    @Override public Observable<ActionState<A>> observeWithReplay() {
        return actionPipe.observe();
    }

    /**
     * {@inheritDoc}
     */
    @Override public Observable<A> observeSuccess() {
        return actionPipe.observeSuccess();
    }

    /**
     * {@inheritDoc}
     */
    @Override public Observable<A> observeSuccessWithReplay() {
        return actionPipe.observeSuccessWithReplay();
    }
}
