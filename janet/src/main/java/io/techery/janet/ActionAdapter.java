package io.techery.janet;

public abstract class ActionAdapter {

    protected Callback callback;

    final <A> void send(A action) {
        try {
            sendInternal(action);
        } catch (Throwable e) {
            if (!(e instanceof JanetServerException)) {
                this.callback.onFail(action, e);
            }
        }
    }

    abstract protected <T> void sendInternal(T action) throws Throwable;

    final void setCallback(Callback callback) {
        this.callback = callback;
    }

    abstract Class getSupportedAnnotationType();

    interface Callback {
        void onStart(Object action);
        void onProgress(Object action, int progress);
        void onSuccess(Object action);
        void onServerError(Object action);
        void onFail(Object action, Throwable throwable);
    }
}
