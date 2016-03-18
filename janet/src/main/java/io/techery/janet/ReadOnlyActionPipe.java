package io.techery.janet;

import rx.Observable;
import rx.functions.Func1;
import rx.internal.util.UtilityFunctions;

/**
 * Read only type of {@linkplain ActionPipe}
 */
public final class ReadOnlyActionPipe<A> implements ReadActionPipe<A> {

    private final ReadActionPipe<A> actionPipe;
    private final Func1<? super A, Boolean> filter;

    private final CachedPipelines<A> cachedPipelines;

    public ReadOnlyActionPipe(ReadActionPipe<A> actionPipe) {
        this(actionPipe, UtilityFunctions.alwaysTrue());
    }

    public ReadOnlyActionPipe(ReadActionPipe<A> actionPipe, Func1<? super A, Boolean> filter) {
        this.actionPipe = actionPipe;
        this.filter = filter;

        cachedPipelines = new CachedPipelines<A>(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Observable<ActionState<A>> observe() {
        return actionPipe.observe()
                .filter(new FilterStateDecorator<A>(filter));
    }

    /**
     * {@inheritDoc}
     */
    @Override public Observable<ActionState<A>> observeWithReplay() {
        return cachedPipelines.observeWithReplay();
    }

    /**
     * {@inheritDoc}
     */
    @Override public Observable<A> observeSuccess() {
        return actionPipe.observeSuccess()
                .filter(filter);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Observable<A> observeSuccessWithReplay() {
        return cachedPipelines.observeSuccessWithReplay();
    }

    /**
     * {@inheritDoc}
     */
    @Override public void clearReplays() {
        cachedPipelines.clearReplays();
    }

    /**
     * Returns a presentation of the ReadOnlyActionPipe with specific predicate
     *
     * @return {@linkplain ReadOnlyActionPipe}
     */
    public ReadOnlyActionPipe<A> filter(Func1<? super A, Boolean> predicate) {
        return new ReadOnlyActionPipe<A>(this, predicate);
    }

    private static class FilterStateDecorator<A> implements Func1<ActionState<A>, Boolean> {

        private final Func1<? super A, Boolean> filter;

        private FilterStateDecorator(Func1<? super A, Boolean> filter) {
            this.filter = filter;
        }

        @Override public Boolean call(ActionState<A> state) {
            return filter.call(state.action);
        }
    }
}
