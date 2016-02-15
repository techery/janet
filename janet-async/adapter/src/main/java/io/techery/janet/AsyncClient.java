package io.techery.janet;

public abstract class AsyncClient {

    protected Callback callback;

    public abstract boolean isConnected();

    public abstract void connect(String url) throws Throwable;

    public abstract void disconnect() throws Throwable;

    public abstract void send(String event, String string) throws Throwable;

    public abstract void send(String event, byte[] bytes) throws Throwable;

    public abstract void subscribe(String event);

    void setCallback(Callback callback) {
        this.callback = callback;
    }

    interface Callback {
        void onConnect();
        void onDisconnect(String reason);
        void onError(Throwable throwable);
        void onMessage(String event, String string);
        void onMessage(String event, byte[] bytes);
    }
}
