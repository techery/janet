package io.techery.janet;

final class CallbackWrapper implements ActionAdapter.Callback {

    private final ActionAdapter.Callback callback;
    private final Interceptor interceptor;

    CallbackWrapper(ActionAdapter.Callback callback, Interceptor interceptor) {
        this.callback = callback;
        this.interceptor = interceptor;
    }

    @Override public void onStart(ActionHolder holder) {
        ActionHolder intercepted = interceptor.interceptStart(holder);
        if (intercepted != null) {
            holder = intercepted;
        }
        callback.onStart(holder);
    }

    @Override public void onProgress(ActionHolder holder, int progress) {
        ActionHolder intercepted = interceptor.interceptProgress(holder, progress);
        if (intercepted != null) {
            holder = intercepted;
        }
        callback.onProgress(holder, progress);
    }

    @Override public void onSuccess(ActionHolder holder) {
        ActionHolder intercepted = interceptor.interceptSuccess(holder);
        if (intercepted != null) {
            holder = intercepted;
        }
        callback.onSuccess(holder);
    }

    @Override public void onFail(ActionHolder holder, JanetException e) {
        ActionHolder intercepted = interceptor.interceptFail(holder, e);
        if (intercepted != null) {
            holder = intercepted;
        }
        callback.onFail(holder, e);
    }

    interface Interceptor {
        <A> ActionHolder<A> interceptStart(ActionHolder<A> holder);
        <A> ActionHolder<A> interceptProgress(ActionHolder<A> holder, int progress);
        <A> ActionHolder<A> interceptSuccess(ActionHolder<A> holder);
        <A> ActionHolder<A> interceptFail(ActionHolder<A> holder, JanetException e);
    }
}