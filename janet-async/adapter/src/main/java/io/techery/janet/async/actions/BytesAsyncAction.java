package io.techery.janet.async.actions;

public abstract class BytesAsyncAction implements SystemAction {

    private final String event;

    public BytesAsyncAction(String event) {
        this.event = event;
    }

    public BytesAsyncAction() {
        this(null);
    }

    public abstract byte[] getBytes();
}
