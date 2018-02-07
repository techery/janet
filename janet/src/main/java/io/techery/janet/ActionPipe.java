package io.techery.janet;

import org.reactivestreams.Publisher;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.techery.janet.ActionState.Status;
import io.techery.janet.helper.ActionStateToActionTransformer;

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

    private final FlowableFactory<A> syncObservableFactory;
    private final Flowable<ActionState<A>> pipeline;
    private final CancelConsumer<A> cancelFunc;
    private final Scheduler defaultSubscribeOn;

    private final CachedPipelines<A> cachedPipelines;
    private final ActiveStream<A> activeStream;

    ActionPipe(
            FlowableFactory<A> syncObservableFactory,
            Provider<Flowable<ActionState<A>>> pipelineFactory,
            CancelConsumer<A> cancelFunc,
            Scheduler defaultSubscribeOn) {
        this.syncObservableFactory = syncObservableFactory;
        this.pipeline = pipelineFactory.provide();
        this.cancelFunc = cancelFunc;
        this.defaultSubscribeOn = defaultSubscribeOn;
        this.cachedPipelines = new CachedPipelines<A>(this);
        this.activeStream = new ActiveStream<A>(this);
    }

    /** {@inheritDoc} */
    @Override public Flowable<ActionState<A>> observe() {
        return pipeline;
    }

    /** {@inheritDoc} */
    @Override public Flowable<ActionState<A>> observeWithReplay() {
        return cachedPipelines.observeWithReplay();
    }

    /** {@inheritDoc} */
    @Override public Flowable<A> observeSuccess() {
        return observe().compose(new ActionSuccessOnlyTransformer<A>());
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
    @Override public void send(A action) {
        send(action, defaultSubscribeOn);
    }

    /** {@inheritDoc} */
    @Override public void send(A action, Scheduler subscribeOn) {
        createObservable(action, subscribeOn).subscribe();
    }

    /** {@inheritDoc} */
    @Override public void cancel(A action) {
        cancelFunc.accept(action);
    }

    /** {@inheritDoc} */
    @Override public void cancelLatest() {
        A action = activeStream.action();
        if (action != null) cancel(action);
    }

    /** {@inheritDoc} */
    @Override public Flowable<ActionState<A>> createObservable(A action) {
        return createObservable(action, defaultSubscribeOn);
    }

    private Flowable<ActionState<A>> createObservable(A action, Scheduler scheduler) {
        activeStream.put(action);
        Flowable observable = syncObservableFactory.create(action);
        if (scheduler != null) {
            observable = observable.subscribeOn(scheduler);
        }
        return observable;
    }

    /** {@inheritDoc} */
    @Override public Single<A> createObservableResult(A action) {
        return createObservable(action).compose(new ActionStateToActionTransformer<A>()).singleOrError();
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
    @Override public ReadActionPipe<A> filter(Predicate<? super A> predicate) {
        return new ReadOnlyActionPipe<A>(this, predicate);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers & Delegates
    ///////////////////////////////////////////////////////////////////////////

    private static final class ActionSuccessOnlyTransformer<T> implements FlowableTransformer<ActionState<T>, T> {

        @Override public Publisher<T> apply(Flowable<ActionState<T>> upstream) {
            return upstream
                    .filter(new Predicate<ActionState<T>>() {
                        @Override public boolean test(ActionState<T> actionState) {
                            return actionState.status == Status.SUCCESS;
                        }
                    })
                    .map(new Function<ActionState<T>, T>() {
                        @Override public T apply(ActionState<T> actionState) {
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
            pipe.observe().doOnNext(new Consumer<ActionState<A>>() {
                @Override public void accept(ActionState<A> as) throws Exception {
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
