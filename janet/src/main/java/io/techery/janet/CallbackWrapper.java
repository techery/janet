package io.techery.janet;

final class CallbackWrapper implements ActionAdapter.Callback {

    private final ActionAdapter.Callback callback;
    private final Interceptor interceptor;

    CallbackWrapper(ActionAdapter.Callback callback, Interceptor interceptor) {
        this.callback = callback;
        this.interceptor = interceptor;
    }

    @Override public void onStart(Object action) {
        callback.onStart(interceptor.onInterceptStart(action));
    }

    @Override public void onProgress(Object action, int progress) {
        callback.onProgress(interceptor.onInterceptProgress(action, progress), progress);
    }

    @Override public void onSuccess(Object action) {
        callback.onSuccess(interceptor.onInterceptSuccess(action));
    }

    @Override public void onFail(Object action, JanetException e) {
        callback.onFail(interceptor.onInterceptFail(action, e), e);
    }

    interface Interceptor {
        <A> A onInterceptStart(A action);
        <A> A onInterceptProgress(A action, int progress);
        <A> A onInterceptSuccess(A action);
        <A> A onInterceptFail(A action, JanetException e);
    }
}