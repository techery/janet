package io.techery.janet;

import rx.Observable;
import rx.observables.ConnectableObservable;

class CachedPipelines<A> implements Replays<A> {

    private final Observable<ActionState<A>> source;
    private final Observable<A> sourceSuccess;

    private ConnectableObservable<ActionState<A>> cachedPipeline;
    private ConnectableObservable<A> cachedSuccessPipeline;

    CachedPipelines(Observable<ActionState<A>> source, Observable<A> sourceSuccess) {
        this.source = source;
        this.sourceSuccess = sourceSuccess;
        createCachedPipeline();
        createCachedSuccessPipeline();
    }

    private void createCachedPipeline() {
        this.cachedPipeline = source
                .replay(1);
        this.cachedPipeline.connect();
    }

    private void createCachedSuccessPipeline() {
        this.cachedSuccessPipeline = sourceSuccess
                .replay(1);
        this.cachedSuccessPipeline.connect();
    }

    @Override public Observable<ActionState<A>> observeWithReplay() {
        return cachedPipeline;
    }

    @Override public Observable<A> observeSuccessWithReplay() {
        return cachedSuccessPipeline;
    }

    @Override public void clearReplays() {
        createCachedPipeline();
        createCachedSuccessPipeline();
    }
}
