package io.techery.janet;

final class CallbackWrapper implements ActionAdapter.Callback {

    private final ActionAdapter.Callback callback;
    private final Interceptor interceptor;

    CallbackWrapper(ActionAdapter.Callback callback, Interceptor interceptor) {
        this.callback = callback;
        this.interceptor = interceptor;
    }

    @Override public void onStart(ActionHolder holder) {
        callback.onStart(interceptor.interceptStart(holder));
    }

    @Override public void onProgress(ActionHolder holder, int progress) {
        callback.onProgress(interceptor.interceptProgress(holder, progress), progress);
    }

    @Override public void onSuccess(ActionHolder holder) {
        callback.onSuccess(interceptor.interceptSuccess(holder));
    }

    @Override public void onFail(ActionHolder holder, JanetException e) {
        callback.onFail(interceptor.interceptFail(holder, e), e);
    }

    interface Interceptor {
        <A> ActionHolder<A> interceptStart(ActionHolder<A> holder);
        <A> ActionHolder<A> interceptProgress(ActionHolder<A> holder, int progress);
        <A> ActionHolder<A> interceptSuccess(ActionHolder<A> holder);
        <A> ActionHolder<A> interceptFail(ActionHolder<A> holder, JanetException e);
    }
}