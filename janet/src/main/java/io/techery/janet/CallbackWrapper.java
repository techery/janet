package io.techery.janet;

final class CallbackWrapper implements ActionService.Callback {

    private final ActionService.Callback callback;
    private final Interceptor interceptor;

    CallbackWrapper(ActionService.Callback callback, Interceptor interceptor) {
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
        boolean intercept = interceptor.interceptFail(holder, e);
        if (intercept) return;
        callback.onFail(holder, e);
    }

    interface Interceptor {
        <A> void interceptStart(ActionHolder<A> holder);
        <A> void interceptProgress(ActionHolder<A> holder, int progress);
        <A> void interceptSuccess(ActionHolder<A> holder);
        <A> boolean interceptFail(ActionHolder<A> holder, JanetException e);
    }
}
