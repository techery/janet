package io.techery.janet;

/**
 * Base class that needs to extend to create a new adapter.
 * Adapter processes Janet operations for supported action type with annotation
 * defined in {@link #getSupportedAnnotationType()}
 */
public abstract class ActionAdapter {

    protected Callback callback;

    final <A> void send(ActionHolder<A> holder) {
        try {
            sendInternal(holder);
        } catch (JanetException e) {
            this.callback.onFail(holder, e);
        }
    }

    /**
     * Action sending
     */
    abstract protected <A> void sendInternal(ActionHolder<A> holder) throws JanetException;

    /**
     * Action cancellation
     */
    abstract protected <A> void cancel(ActionHolder<A> holder);

    /**
     * Getting action annotation type for using to create supported action.
     * Actions with this annotation will be processed by the ActionAdapter.
     */
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
