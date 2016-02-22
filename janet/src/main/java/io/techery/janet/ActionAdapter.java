package io.techery.janet;

public abstract class ActionAdapter {

    protected Callback callback;

    final <A> void send(A action) {
        try {
            sendInternal(action);
        } catch (JanetException e) {
            this.callback.onFail(action, e);
        }
    }

    final <A> void cancel(A action) {
        try {
            cancelInternal(action);
        } catch (JanetException e) {
            this.callback.onFail(action, e);
        }
    }

    protected abstract <A> void cancelInternal(A action) throws JanetException;

    abstract protected <T> void sendInternal(T action) throws JanetException;

    final void setCallback(Callback callback) {
        this.callback = callback;
    }

    abstract Class getSupportedAnnotationType();

    interface Callback {
        void onStart(Object action);
        void onProgress(Object action, int progress);
        void onSuccess(Object action);
        void onFail(Object action, JanetException e);
    }
}
