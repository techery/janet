package io.techery.janet;

import io.reactivex.Flowable;
import io.reactivex.functions.Predicate;
import io.reactivex.internal.functions.Functions;

/**
 * Read only type of {@linkplain ActionPipe}
 */
public final class ReadOnlyActionPipe<A> implements ReadActionPipe<A> {

    private final ReadActionPipe<A> actionPipe;
    private final Predicate<? super A> filter;

    private final CachedPipelines<A> cachedPipelines;

    public ReadOnlyActionPipe(ReadActionPipe<A> actionPipe) {
        this(actionPipe, Functions.alwaysTrue());
    }

    public ReadOnlyActionPipe(ReadActionPipe<A> actionPipe, Predicate<? super A> filter) {
        this.actionPipe = actionPipe;
        this.filter = filter;
        cachedPipelines = new CachedPipelines<A>(this);
    }

    /** {@inheritDoc} */
    @Override public Flowable<ActionState<A>> observe() {
        return actionPipe.observe().filter(new FilterStateDecorator<A>(filter));
    }

    /** {@inheritDoc} */
    @Override public Flowable<ActionState<A>> observeWithReplay() {
        return cachedPipelines.observeWithReplay();
    }

    /** {@inheritDoc} */
    @Override public Flowable<A> observeSuccess() {
        return actionPipe.observeSuccess().filter(filter);
    }

    /** {@inheritDoc} */
    @Override public Flowable<A> observeSuccessWithReplay() {
        return cachedPipelines.observeSuccessWithReplay();
    }

    /** {@inheritDoc} */
    @Override public void clearReplays() {
        cachedPipelines.clearReplays();
    }

    /** {@inheritDoc} */
    @Override public ReadOnlyActionPipe<A> filter(Predicate<? super A> predicate) {
        return new ReadOnlyActionPipe<A>(this, predicate);
    }

    static class FilterStateDecorator<A> implements Predicate<ActionState<A>> {

        private final Predicate<? super A> filter;

        private FilterStateDecorator(Predicate<? super A> filter) {
            this.filter = filter;
        }

        @Override public boolean test(ActionState<A> state) throws Exception {
            return filter.test(state.action);
        }
    }
}
