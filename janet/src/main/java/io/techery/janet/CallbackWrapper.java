package io.techery.janet;

final class CallbackWrapper implements ActionAdapter.Callback {

    private final ActionAdapter.Callback callback;
    private final Interceptor interceptor;

    CallbackWrapper(ActionAdapter.Callback callback, Interceptor interceptor) {
        this.callback = callback;
        this.interceptor = interceptor;
    }

    @Override public void onStart(ActionHolder holder) {
        interceptor.interceptStart(holder);
        callback.onStart(holder);
    }

    @Override public void onProgress(ActionHolder holder, int progress) {
        interceptor.interceptProgress(holder, progress);
        callback.onProgress(holder, progress);
    }

    @Override public void onSuccess(ActionHolder holder) {
        interceptor.interceptSuccess(holder);
        callback.onSuccess(holder);
    }

    @Override public void onFail(ActionHolder holder, JanetException e) {
        interceptor.interceptFail(holder, e);
        callback.onFail(holder, e);
    }

    interface Interceptor {
        <A> void interceptStart(ActionHolder<A> holder);
        <A> void interceptProgress(ActionHolder<A> holder, int progress);
        <A> void interceptSuccess(ActionHolder<A> holder);
        <A> void interceptFail(ActionHolder<A> holder, JanetException e);
    }
}