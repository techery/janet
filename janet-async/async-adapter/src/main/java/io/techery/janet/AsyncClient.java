package io.techery.janet;

public abstract class AsyncClient {

    protected Callback callback;

    protected abstract boolean isConnected();

    protected abstract void connect(String url, boolean reconnectIfConnected) throws Throwable;

    protected abstract void disconnect() throws Throwable;

    protected abstract void send(String event, String string) throws Throwable;

    protected abstract void send(String event, byte[] bytes) throws Throwable;

    protected abstract void subscribe(String event);

    void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void onConnect();
        void onDisconnect(String reason);
        void onError(Throwable throwable);
        void onMessage(String event, String string);
        void onMessage(String event, byte[] bytes);
    }
}
