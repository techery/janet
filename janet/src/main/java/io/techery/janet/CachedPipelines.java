package io.techery.janet;


import org.reactivestreams.Publisher;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.functions.Predicate;
import io.reactivex.processors.PublishProcessor;

class CachedPipelines<A> implements Replays<A> {

    private final Flowable<ActionState<A>> source;
    private final Flowable<A> sourceSuccess;

    private Flowable<ActionState<A>> cachedPipeline;
    private Flowable<A> cachedSuccessPipeline;

    private final PublishProcessor clearingStream;

    CachedPipelines(ReadActionPipe<A> actionPipe) {
        this.source = actionPipe.observe();
        this.sourceSuccess = actionPipe.observeSuccess();
        this.clearingStream = PublishProcessor.create();
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

    private <A> Flowable<A> createPipeline(Flowable<A> source) {
        return source.mergeWith(clearingStream).replay(1).autoConnect();
    }

    @Override public Flowable<ActionState<A>> observeWithReplay() {
        return cachedPipeline.compose(EmptyFilter.<ActionState<A>>instance());
    }

    @Override public Flowable<A> observeSuccessWithReplay() {
        return cachedSuccessPipeline.compose(EmptyFilter.<A>instance());
    }

    @Override public void clearReplays() {
        clearingStream.onNext(ClearSignal.IT);
    }

    private static class EmptyFilter<T> implements FlowableTransformer<T, T> {

        private static final EmptyFilter INSTANCE = new EmptyFilter();

        public static <T> EmptyFilter<T> instance() {
            return (EmptyFilter<T>) INSTANCE;
        }

        @Override public Publisher<T> apply(Flowable<T> upstream) {
            return upstream.filter(new Predicate<T>() {
                @Override public boolean test(T t) throws Exception {
                    return t != ClearSignal.IT;
                }
            });
        }
    }

    enum ClearSignal {IT}
}
