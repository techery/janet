package io.techery.janet;

public abstract class ActionAdapter {

    protected Callback callback;

    final <A> void send(ActionHolder<A> holder) {
        try {
            sendInternal(holder);
        } catch (JanetException e) {
            this.callback.onFail(holder, e);
        }
    }

    abstract protected <A> void sendInternal(ActionHolder<A> holder) throws JanetException;

    abstract protected <A> void cancel(ActionHolder<A> holder);

    abstract protected Class getSupportedAnnotationType();

    void setCallback(Callback callback) {
        this.callback = callback;
    }

    interface Callback {
        void onStart(ActionHolder action);
        void onProgress(ActionHolder action, int progress);
        void onSuccess(ActionHolder action);
        void onFail(ActionHolder action, JanetException e);
    }
}
