package io.techery.janet;

final class CallbackWrapper implements ActionAdapter.Callback {

    private final ActionAdapter.Callback callback;
    private final Interceptor interceptor;

    CallbackWrapper(ActionAdapter.Callback callback, Interceptor interceptor) {
        this.callback = callback;
        this.interceptor = interceptor;
    }

    @Override public void onStart(Object action) {
        callback.onStart(interceptor.interceptStart(action));
    }

    @Override public void onProgress(Object action, int progress) {
        callback.onProgress(interceptor.interceptProgress(action, progress), progress);
    }

    @Override public void onSuccess(Object action) {
        callback.onSuccess(interceptor.interceptSuccess(action));
    }

    @Override public void onFail(Object action, JanetException e) {
        callback.onFail(interceptor.interceptFail(action, e), e);
    }

    interface Interceptor {
        <A> A interceptStart(A action);
        <A> A interceptProgress(A action, int progress);
        <A> A interceptSuccess(A action);
        <A> A interceptFail(A action, JanetException e);
    }
}