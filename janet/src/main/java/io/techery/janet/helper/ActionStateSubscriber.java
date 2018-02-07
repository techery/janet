package io.techery.janet.helper;

import io.reactivex.exceptions.OnErrorNotImplementedException;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.subscribers.DisposableSubscriber;
import io.techery.janet.ActionState;

import static io.techery.janet.ActionState.Status.FAIL;
import static io.techery.janet.ActionState.Status.SUCCESS;

/**
 * Subscriber that helps to handle states by status using callback
 */
public class ActionStateSubscriber<A> extends DisposableSubscriber<ActionState<A>> {

    private Consumer<A> onSuccess;
    private BiConsumer<A, Throwable> onFail;
    private Consumer<A> onStart;
    private BiConsumer<A, Integer> onProgress;
    private Consumer<ActionState<A>> beforeEach;
    private Consumer<ActionState<A>> afterEach;
    private Consumer<A> onFinish;

    public ActionStateSubscriber<A> onSuccess(Consumer<A> onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    public ActionStateSubscriber<A> onFail(BiConsumer<A, Throwable> onError) {
        this.onFail = onError;
        return this;
    }

    public ActionStateSubscriber<A> onFinish(Consumer<A> onFinish) {
        this.onFinish = onFinish;
        return this;
    }

    public ActionStateSubscriber<A> onStart(Consumer<A> onStart) {
        this.onStart = onStart;
        return this;
    }

    public ActionStateSubscriber<A> onProgress(BiConsumer<A, Integer> onProgress) {
        this.onProgress = onProgress;
        return this;
    }

    public ActionStateSubscriber<A> beforeEach(Consumer<ActionState<A>> onEach) {
        this.beforeEach = onEach;
        return this;
    }

    public ActionStateSubscriber<A> afterEach(Consumer<ActionState<A>> afterEach) {
        this.afterEach = afterEach;
        return this;
    }

    @Override public void onNext(ActionState<A> state) {
        try {
            if (beforeEach != null)
                beforeEach.accept(state);
            switch (state.status) {
                case START:
                    if (onStart != null) onStart.accept(state.action);
                    break;
                case PROGRESS:
                    if (onProgress != null) onProgress.accept(state.action, state.progress);
                    break;
                case SUCCESS:
                    if (onSuccess != null) onSuccess.accept(state.action);
                    break;
                case FAIL:
                    if (onFail != null) onFail.accept(state.action, state.exception);
                    break;
            }
            if (onFinish != null && (state.status == SUCCESS || state.status == FAIL)) {
                onFinish.accept(state.action);
            }
            if (afterEach != null) afterEach.accept(state);
        } catch (Exception e) {
            onError(e);
        }
    }

    @Override public void onError(Throwable e) {
        throw new OnErrorNotImplementedException(e);
    }

    @Override public void onComplete() {
    }
}
