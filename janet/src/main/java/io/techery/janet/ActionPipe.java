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

    private final CachedPipelines<A> cachedPipelines;
    private final ActiveStream<A> activeStream;

    ActionPipe(Func1<A, Observable<ActionState<A>>> syncObservableFactory,
            Func0<Observable<ActionState<A>>> pipelineFactory,
            final Action1<A> cancelFunc,
            Scheduler defaultSubscribeOn) {
        this.syncObservableFactory = syncObservableFactory;
        this.pipeline = pipelineFactory.call();
        this.cancelFunc = cancelFunc;
        this.defaultSubscribeOn = defaultSubscribeOn;
        this.cachedPipelines = new CachedPipelines<A>(this);
        this.activeStream = new ActiveStream<A>(this);
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
        send(action, defaultSubscribeOn);
    }

    /** {@inheritDoc} */
    @Override public void send(A action, Scheduler subscribeOn) {
        createObservable(action, subscribeOn).subscribe();
    }

    /** {@inheritDoc} */
    @Override public void cancel(A action) {
        cancelFunc.call(action);
    }

    /** {@inheritDoc} */
    @Override public void cancelLatest() {
        Observable.just(activeStream.action())
                .filter(new Func1<A, Boolean>() {
                    @Override public Boolean call(A a) {
                        return a != null;
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
        return createObservable(action, defaultSubscribeOn);
    }

    private Observable<ActionState<A>> createObservable(A action, Scheduler scheduler) {
        activeStream.put(action);
        Observable observable = syncObservableFactory.call(action);
        if (scheduler != null) {
            observable = observable.subscribeOn(scheduler);
        }
        return observable;
    }

    /** {@inheritDoc} */
    @Override public Observable<A> createObservableResult(A action) {
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

    ///////////////////////////////////////////////////////////////////////////
    // Helpers & Delegates
    ///////////////////////////////////////////////////////////////////////////

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

    private static final class ActiveStream<A> {
        private volatile A action;

        public ActiveStream(ReadActionPipe<A> pipe) {
            connectPipe(pipe);
        }

        private void connectPipe(ReadActionPipe<A> pipe) {
            pipe.observe().doOnNext(new Action1<ActionState<A>>() {
                @Override public void call(ActionState<A> as) {
                    if (as.status == Status.START || as.status == Status.PROGRESS) {
                        put(as.action); // update cache with latest active action
                    } else if (as.action == action) {
                        put(null); // cleanup cache if latest action is finished
                    }
                }
            }).subscribe();
        }

        public void put(A action) {
            this.action = action;
        }

        /** Connect to non-finished actions cache (get latest) */
        public A action() {
            return action;
        }
    }
}
