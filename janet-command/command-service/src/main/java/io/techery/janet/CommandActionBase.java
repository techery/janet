package io.techery.janet;

/**
 * Base class which contains the command's methods for running and cancellation.
 * To get command result use method getResult().
 */
public abstract class CommandActionBase<T> {

    private T result;
    private boolean canceled;

    /**
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever. This method is invoked on action send.
     *
     * @param callback - callback to send command state
     * @throws Throwable - any exceptions will be handled and sent to client as status {@linkplain ActionState.Status#FAIL}
     */
    protected abstract void run(CommandCallback<T> callback) throws Throwable;

    /**
     * For cancellation logic
     */
    public void cancel() {
        //do nothing
    }

    final public T getResult() {
        return result;
    }

    final void setResult(T result) {
        this.result = result;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    protected interface CommandCallback<T> {
        void onProgress(int progress);
        void onSuccess(T result);
        void onFail(Throwable throwable);
    }
}
