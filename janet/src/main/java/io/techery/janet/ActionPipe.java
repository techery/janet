package io.techery.janet;

import io.techery.janet.helper.ActionStateToActionTransformer;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.subjects.PublishSubject;

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
final public class ActionPipe<A> {
    private final Func1<A, Observable<ActionState<A>>> syncObservableFactory;
    private final Observable<ActionState<A>> pipeline;
    private final Action1<A> cancelFunc;
    private final Scheduler defaultSubscribeOn;
    private final PublishSubject<A> cancelSignal;

    private ConnectableObservable<ActionState<A>> cachedPipeline;
    private ConnectableObservable<A> cachedSuccessPipeline;

    ActionPipe(Func1<A, Observable<ActionState<A>>> syncObservableFactory,
            Func0<Observable<ActionState<A>>> pipelineFactory,
            Action1<A> cancelFunc,
            Scheduler defaultSubscribeOn) {
        this.syncObservableFactory = syncObservableFactory;
        this.pipeline = pipelineFactory.call();
        this.cancelFunc = cancelFunc;
        this.defaultSubscribeOn = defaultSubscribeOn;
        this.cancelSignal = PublishSubject.create();

        createCachedPipeline();
        createCachedSuccessPipeline();
    }

    private void createCachedPipeline() {
        this.cachedPipeline = pipeline.replay(1);
        this.cachedPipeline.connect();
    }

    private void createCachedSuccessPipeline() {
        this.cachedSuccessPipeline = pipeline
                .compose(new ActionSuccessOnlyTransformer<A>())
                .replay(1);
        this.cachedSuccessPipeline.connect();
    }

    /**
     * Observe all states of specified action type
     */
    public Observable<ActionState<A>> observe() {
        return pipeline;
    }

    /**
     * Observe all states of specified action type with cache.
     * Last action state will be emitted immediately after subscribe.
     *
     * @see Observable#replay(int)
     */
    public Observable<ActionState<A>> observeWithReplay() {
        return cachedPipeline.asObservable();
    }

    /**
     * Observe success action only, if you want to catch any exceptions, use {@link ActionPipe#observe()}
     */
    public Observable<A> observeSuccess() {
        return observe()
                .compose(new ActionSuccessOnlyTransformer<A>());
    }

    /**
     * Observe action result with cache.
     * Emmit the latest result, if exist, immediately after subscribe.
     *
     * @see Observable#replay(int)
     * @see ActionPipe#observeSuccess()
     */
    public Observable<A> observeSuccessWithReplay() {
        return cachedSuccessPipeline.asObservable();
    }

    /**
     * Clear cached action
     */
    public void clearReplays() {
        createCachedPipeline();
    }

    /**
     * Send action to {@linkplain Janet}.
     *
     * @param action prepared action for sending
     */
    public void send(A action) {
        send(action, null);
    }

    /**
     * Send action to {@linkplain Janet}.
     *
     * @param action      prepared action for sending
     * @param subscribeOn {@linkplain Scheduler} to do {@linkplain Observable#subscribeOn(Scheduler) subcribeOn} of created Observable.
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
     * Cancel running action.
     * Action cancellation defines in relative service {@linkplain ActionService#cancel(ActionHolder)}
     *
     * @param action prepared action for cancellation
     */
    public void cancel(A action) {
        cancelSignal.onNext(action);
        cancelFunc.call(action);
    }

    /**
     * Create {@linkplain Observable observable} to send action and receive result
     * in the form of action {@linkplain ActionState states} synchronously
     *
     * @param action prepared action to send
     */
    public Observable<ActionState<A>> createObservable(final A action) {
        return syncObservableFactory.call(action)
                .takeUntil(cancelSignal
                        .asObservable()
                        .filter(new Func1<A, Boolean>() {
                            @Override public Boolean call(A a) {
                                return a == action;
                            }
                        }));
    }

    /**
     * Create {@linkplain Observable observable} to send action and receive action with result synchronously
     * <p>
     * To catch errors use {@linkplain Subscriber#onError(Throwable)}
     *
     * @param action prepared action to send
     */
    public Observable<A> createActionObservable(A action) {
        return createObservable(action).compose(new ActionStateToActionTransformer<A>());
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