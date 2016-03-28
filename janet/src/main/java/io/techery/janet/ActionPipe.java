package io.techery.janet;

import io.techery.janet.ActionState.Status;
import io.techery.janet.helper.ActionStateToActionTransformer;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;

/**
 * End tool for sending and receiving actions with specific type using RXJava.
 * ActionPipe can work with actions synchronously or asynchronously.
 * Create instances using method {@linkplain Janet#createPipe(Class)}.
 * <p>
 * For example,
 * <pre>{@code
 * ActionPipe<UsersAction> usersPipe = janet.createPipe(UsersAction.class);}
 * </pre>
 */
public final class ActionPipe<A> implements ReadActionPipe<A>, WriteActionPipe<A> {
    private final Func1<A, Observable<ActionState<A>>> syncObservableFactory;
    private final Observable<ActionState<A>> pipeline;
    private final Action1<A> cancelFunc;
    private final Scheduler defaultSubscribeOn;

    private CachedPipelines<A> cachedPipelines;

    ActionPipe(Func1<A, Observable<ActionState<A>>> syncObservableFactory,
            Func0<Observable<ActionState<A>>> pipelineFactory,
            Action1<A> cancelFunc,
            Scheduler defaultSubscribeOn) {
        this.syncObservableFactory = syncObservableFactory;
        this.pipeline = pipelineFactory.call();
        this.cancelFunc = cancelFunc;
        this.defaultSubscribeOn = defaultSubscribeOn;
        this.cachedPipelines = new CachedPipelines<A>(this);
    }

    /** {@inheritDoc} */
    @Override public Observable<ActionState<A>> observe() {
        return pipeline;
    }

    /** {@inheritDoc} */
    @Override public Observable<ActionState<A>> observeWithReplay() {
        return cachedPipelines.observeWithReplay();
    }

    /** {@inheritDoc} */
    @Override public Observable<A> observeSuccess() {
        return observe().compose(new ActionSuccessOnlyTransformer<A>());
    }

    /** {@inheritDoc} */
    @Override public Observable<A> observeSuccessWithReplay() {
        return cachedPipelines.observeSuccessWithReplay();
    }

    /** {@inheritDoc} */
    @Override public void clearReplays() {
        cachedPipelines.clearReplays();
    }

    /** {@inheritDoc} */
    @Override public void send(A action) {
        send(action, null);
    }

    /** {@inheritDoc} */
    @Override public void send(A action, Scheduler subscribeOn) {
        Observable observable = createObservable(action);
        if (subscribeOn == null) {
            subscribeOn = defaultSubscribeOn;
        }
        if (subscribeOn != null) {
            observable = observable.subscribeOn(subscribeOn);
        }
        observable.subscribe();
    }

    /** {@inheritDoc} */
    @Override public void cancel(A action) {
        cancelFunc.call(action);
    }

    /** {@inheritDoc} */
    @Override public void cancelLatest() {
        cachedPipelines.observeWithReplay().take(1)
                .filter(new Func1<ActionState<A>, Boolean>() {
                    @Override public Boolean call(ActionState<A> as) {
                        return as.status == Status.START || as.status == Status.PROGRESS;
                    }
                })
                .map(new Func1<ActionState<A>, A>() {
                    @Override public A call(ActionState<A> as) {
                        return as.action;
                    }
                })
                .subscribe(new Action1<A>() {
                    @Override public void call(A a) {
                        cancel(a);
                    }
                });
    }

    /** {@inheritDoc} */
    @Override public Observable<ActionState<A>> createObservable(A action) {
        return syncObservableFactory.call(action);
    }

    /** {@inheritDoc} */
    @Override public Observable<A> createObservableSuccess(A action) {
        return createObservable(action).compose(new ActionStateToActionTransformer<A>());
    }

    /**
     * Returns a presentation of the ActionPipe with read only mod
     *
     * @return {@linkplain ReadOnlyActionPipe}
     */
    public ReadOnlyActionPipe<A> asReadOnly() {
        return new ReadOnlyActionPipe<A>(this);
    }

    /** {@inheritDoc} */
    @Override public ReadActionPipe<A> filter(Func1<? super A, Boolean> predicate) {
        return new ReadOnlyActionPipe<A>(this, predicate);
    }

    private static final class ActionSuccessOnlyTransformer<T> implements Observable.Transformer<ActionState<T>, T> {
        @Override public Observable<T> call(Observable<ActionState<T>> actionStateObservable) {
            return actionStateObservable
                    .filter(new Func1<ActionState<T>, Boolean>() {
                        @Override public Boolean call(ActionState<T> actionState) {
                            return actionState.status == Status.SUCCESS;
                        }
                    })
                    .map(new Func1<ActionState<T>, T>() {
                        @Override public T call(ActionState<T> actionState) {
                            return actionState.action;
                        }
                    });
        }
    }
}