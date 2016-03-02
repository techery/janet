package io.techery.janet;

public abstract class CommandActionBase<T> {

    private T result;
    private boolean canceled;

    protected abstract void run(CommandCallback<T> callback) throws Throwable;

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
