package io.techery.janet;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;

final public class JanetExecutor<A> {

    private final Func1<A, Observable<ActionState<A>>> syncObservableFactory;
    private final Observable<ActionState<A>> pipeline;
    private ConnectableObservable<ActionState<A>> cachedPipeline;
    private Scheduler subscribeOn;

    JanetExecutor(Func1<A, Observable<ActionState<A>>> syncObservableFactory, Func0<Observable<ActionState<A>>> pipelineFactory) {
        this.syncObservableFactory = syncObservableFactory;
        this.pipeline = pipelineFactory.call();
        createCachedPipeline();
    }

    private void createCachedPipeline() {
        this.cachedPipeline = pipeline.replay(1);
        this.cachedPipeline.connect();
    }

    public Observable<ActionState<A>> observe() {
        return pipeline;
    }

    public Observable<ActionState<A>> observeWithReplay() {
        return cachedPipeline;
    }

    public Observable<A> observeActions() {
        return observe()
                .compose(new StateToAction<A>());

    }

    public Observable<A> observeActionsWithReplay() {
        return observeWithReplay()
                .compose(new StateToAction<A>());
    }

    public void clearReplays() {
        createCachedPipeline();
    }

    @Deprecated //TODO: Is it need ?
    public void execute(A action) {
        createObservable(action).subscribe();
    }

    public JanetExecutor<A> scheduler(Scheduler scheduler) {
        this.subscribeOn = scheduler;
        return this;
    }

    public Observable<A> createActionsObservable(A action) {
        return createObservable(action).compose(new StateToAction<A>());
    }

    public Observable<ActionState<A>> createObservable(final A action) {
        return Observable.defer(new Func0<Observable<ActionState<A>>>() {
            @Override
            public Observable<ActionState<A>> call() {
                return syncObservableFactory.call(action);
            }
        }).compose(new Observable.Transformer<ActionState<A>, ActionState<A>>() {
            @Override
            public Observable<ActionState<A>> call(Observable<ActionState<A>> observable) {
                if (subscribeOn != null)
                    observable = observable.subscribeOn(subscribeOn);
                return observable;
            }
        });
    }

}