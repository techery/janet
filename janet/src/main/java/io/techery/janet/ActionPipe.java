package io.techery.janet;

import io.techery.janet.helper.ActionStateToActionTransformer;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;

/**
 * End tool for sending and receiving actions with specific type using RXJava.
 * ActionPipe can work with actions synchronously or asynchronously.
 * Create instances using method {@link Janet#createPipe(Class)}.
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

    private ConnectableObservable<ActionState<A>> cachedPipeline;
    private ConnectableObservable<A> cachedSuccessPipeline;

    private Scheduler subscribeOn;

    ActionPipe(Func1<A, Observable<ActionState<A>>> syncObservableFactory, Func0<Observable<ActionState<A>>> pipelineFactory, Action1<A> cancelFunc) {
        this.syncObservableFactory = syncObservableFactory;
        this.pipeline = pipelineFactory.call();
        this.cancelFunc = cancelFunc;

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
        return cachedPipeline;
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
        return cachedSuccessPipeline;
    }

    /**
     * Clear cached action
     */
    public void clearReplays() {
        createCachedPipeline();
    }

    /**
     * Send action to {@link Janet}.
     *
     * @param action prepared action for sending
     */
    public void send(A action) {
        createObservable(action).subscribe();
    }

    /**
     * Cancel running action.
     * Action cancellation defines in relative service {@link ActionService#cancel(ActionHolder)}
     *
     * @param action prepared action for cancellation
     */
    public void cancel(A action) {
        cancelFunc.call(action);
    }

    /**
     * {@link Scheduler} to do {@link Observable#subscribeOn(Scheduler) subcribeOn} of created Observable.
     */
    public ActionPipe<A> pimp(Scheduler scheduler) {
        this.subscribeOn = scheduler;
        return this;
    }

    /**
     * Create {@link Observable observable} to send action and receive result
     * in the form of action {@link ActionState states} synchronously
     *
     * @param action prepared action to send
     */
    public Observable<ActionState<A>> createObservable(final A action) {
        return syncObservableFactory.call(action)
                .compose(new Observable.Transformer<ActionState<A>, ActionState<A>>() {
                    @Override
                    public Observable<ActionState<A>> call(Observable<ActionState<A>> observable) {
                        if (subscribeOn != null)
                            observable = observable.subscribeOn(subscribeOn);
                        return observable;
                    }
                });
    }

    /**
     * Create {@link Observable observable} to send action and receive action with result synchronously
     * <p>
     * To catch errors use {@link Subscriber#onError(Throwable)}
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