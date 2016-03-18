package io.techery.janet;

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
        this.cachedPipelines = new CachedPipelines<A>(observe(), observeSuccess());
    }

    /**
     * {@inheritDoc}
     */
    public Observable<ActionState<A>> observe() {
        return pipeline;
    }

    /**
     * {@inheritDoc}
     */
    public Observable<ActionState<A>> observeWithReplay() {
        return cachedPipelines.observeWithReplay();
    }

    /**
     * {@inheritDoc}
     */
    public Observable<A> observeSuccess() {
        return observe()
                .compose(new ActionSuccessOnlyTransformer<A>());
    }

    /**
     * {@inheritDoc}
     */
    public Observable<A> observeSuccessWithReplay() {
        return cachedPipelines.observeSuccessWithReplay();
    }

    /**
     * Clear cached action
     */
    public void clearReplays() {
        cachedPipelines.clearReplays();
    }

    /**
     * {@inheritDoc}
     */
    public void send(A action) {
        send(action, null);
    }

    /**
     * {@inheritDoc}
     */
    public void send(A action, Scheduler subscribeOn) {
        Observable observable = createObservable(action);
        if (subscribeOn == null) {
            subscribeOn = defaultSubscribeOn;
        }
        if (subscribeOn != null) {
            observable = observable.subscribeOn(subscribeOn);
        }
        observable.subscribe();
    }

    /**
     * {@inheritDoc}
     */
    public void cancel(A action) {
        cancelFunc.call(action);
    }

    /**
     * {@inheritDoc}
     */
    public Observable<ActionState<A>> createObservable(final A action) {
        return syncObservableFactory.call(action);
    }

    /**
     * {@inheritDoc}
     */
    public Observable<A> createObservableSuccess(A action) {
        return createObservable(action).compose(new ActionStateToActionTransformer<A>());
    }

    /**
     * Returns a presentation of the ActionPipe with read only mod
     *
     * @return {@linkplain ReadOnlyActionPipe}
     */
    public ReadOnlyActionPipe<A> toReadOnly() {
        return new ReadOnlyActionPipe<A>(this);
    }

    private static final class ActionSuccessOnlyTransformer<T> implements Observable.Transformer<ActionState<T>, T> {
        @Override public Observable<T> call(Observable<ActionState<T>> actionStateObservable) {
            return actionStateObservable
                    .filter(new Func1<ActionState<T>, Boolean>() {
                        @Override public Boolean call(ActionState<T> actionState) {
                            return actionState.status == ActionState.Status.SUCCESS;
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