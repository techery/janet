package io.techery.janet;

import io.techery.janet.helper.ActionStateToActionTransformer;
import rx.Observable;
import rx.Scheduler;
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
    private Scheduler subscribeOn;

    ActionPipe(Func1<A, Observable<ActionState<A>>> syncObservableFactory, Func0<Observable<ActionState<A>>> pipelineFactory, Action1<A> cancelFunc) {
        this.syncObservableFactory = syncObservableFactory;
        this.pipeline = pipelineFactory.call();
        this.cancelFunc = cancelFunc;
        createCachedPipeline();
    }

    private void createCachedPipeline() {
        this.cachedPipeline = pipeline.replay(1);
        this.cachedPipeline.connect();
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
     * Observe successful actions only
     */
    public Observable<A> observeSuccess() {
        return observe()
                .compose(new ActionStateToActionTransformer<A>());

    }

    /**
     * Observe successful actions only with cache.
     * Last action state will be emitted immediately after subscribe.
     *
     * @see Observable#replay(int)
     */
    public Observable<A> observeSuccessWithReplay() {
        return observeWithReplay()
                .compose(new ActionStateToActionTransformer<A>());
    }

    /**
     * Clear cached action emits
     */
    public void clearReplays() {
        createCachedPipeline();
    }

    /**
     * Send action to {@link Janet}.
     * Uses relative adapter {@link ActionAdapter#sendInternal(Object)}
     *
     * @param action prepared action for sending
     */
    public void send(A action) {
        createObservable(action).subscribe();
    }

    /**
     * Cancel running action.
     * Action cancellation defines in relative adapter {@link ActionAdapter#cancel(Object)}
     *
     * @param action prepared action for cancellation
     */
    public void cancel(A action) {
        cancelFunc.call(action);
    }

    public ActionPipe<A> pimp(Scheduler scheduler) {
        this.subscribeOn = scheduler;
        return this;
    }

    public Observable<A> createSuccessObservable(A action) {
        return createObservable(action).compose(new ActionStateToActionTransformer<A>());
    }

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

}