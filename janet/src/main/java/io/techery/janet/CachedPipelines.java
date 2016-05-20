package io.techery.janet;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

class CachedPipelines<A> implements Replays<A> {

    private final Observable<ActionState<A>> source;
    private final Observable<A> sourceSuccess;

    private Observable<ActionState<A>> cachedPipeline;
    private Observable<A> cachedSuccessPipeline;

    private final PublishSubject clearingStream;

    CachedPipelines(ReadActionPipe<A> actionPipe) {
        this.source = actionPipe.observe();
        this.sourceSuccess = actionPipe.observeSuccess();
        this.clearingStream = PublishSubject.create();
        createCachedPipeline();
        createCachedSuccessPipeline();
    }

    private void createCachedPipeline() {
        this.cachedPipeline = createPipeline(source);
        this.cachedPipeline.subscribe();
    }

    private void createCachedSuccessPipeline() {
        this.cachedSuccessPipeline = createPipeline(sourceSuccess);
        this.cachedSuccessPipeline.subscribe();
    }

    private <A> Observable<A> createPipeline(Observable<A> source) {
        return source.mergeWith(clearingStream).replay(1).autoConnect();
    }

    @Override public Observable<ActionState<A>> observeWithReplay() {
        return cachedPipeline.compose(NullFilter.<ActionState<A>>instance());
    }

    @Override public Observable<A> observeSuccessWithReplay() {
        return  cachedSuccessPipeline.compose(NullFilter.<A>instance());
    }

    @Override public void clearReplays() {
        clearingStream.onNext(null);
    }

    private static class NullFilter<T> implements Observable.Transformer<T, T> {

        private static final NullFilter INSTANCE = new NullFilter();

        public static <T> NullFilter<T> instance() {
            return (NullFilter<T>) INSTANCE;
        }

        @Override public Observable<T> call(Observable<T> source) {
            return source.filter(new Func1<Object, Boolean>() {
                @Override public Boolean call(Object obj) {
                    return obj != null;
                }
            });
        }
    }
}
