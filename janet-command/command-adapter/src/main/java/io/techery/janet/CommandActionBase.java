package io.techery.janet;

public abstract class CommandActionBase<T> {

    private T result;

    protected abstract T run(CommandCallback callback) throws Throwable;

    public void cancel() {
        //do nothing
    }

    final public T getResult() {
        return result;
    }

    final void setResult(T result) {
        this.result = result;
    }

    protected interface CommandCallback {
        void onProgress(int progress);
    }
}
