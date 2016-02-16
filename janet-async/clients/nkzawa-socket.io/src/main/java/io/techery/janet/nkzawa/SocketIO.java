package io.techery.janet.nkzawa;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import io.techery.janet.AsyncClient;

public class SocketIO extends AsyncClient {

    private Socket socket;

    public SocketIO() {

    }

    @Override public boolean isConnected() {
        return socket != null && socket.connected();
    }

    @Override public void connect(final String url, final boolean reconnectIfConnected) throws Throwable {
        if (isConnected()) {
            if (reconnectIfConnected) {
                socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                    @Override public void call(Object... args) {
                        try {
                            connect(url, reconnectIfConnected);
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    }
                });
                socket.disconnect();
                socket = null;
            } else {
                callback.onConnect();
            }
            return;
        }
        socket = IO.socket(url);
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override public void call(Object... args) {
                callback.onConnect();
            }
        });
        socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override public void call(Object... args) {
                String reason = null;
                if (args.length > 0) {
                    reason = String.valueOf(args[0]);
                }
                callback.onDisconnect(reason);
            }
        });
        socket.on(Socket.EVENT_ERROR, new Emitter.Listener() {
            @Override public void call(Object... args) {
                Throwable throwable = null;
                if (args.length > 0) {
                    throwable = (Throwable) args[0];
                }
                callback.onError(throwable);
            }
        });
        socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override public void call(Object... args) {
                Throwable throwable = null;
                if (args.length > 0) {
                    throwable = (Throwable) args[0];
                }
                callback.onError(throwable);
            }
        });
    }

    @Override public void disconnect() throws Throwable {
        if (isConnected()) {
            socket.disconnect();
        } else {
            callback.onDisconnect("not connected");
        }
    }

    @Override public void send(String event, String string) throws Throwable {
        if (isConnected()) {
            socket.emit(event, string);
        }
    }

    @Override public void send(String event, byte[] bytes) throws Throwable {
        if (isConnected()) {
            socket.emit(event, new Object[]{bytes});
        }
    }

    @Override public void subscribe(final String event) {
        if (isConnected()) {
            socket.on(event, new Emitter.Listener() {
                @Override public void call(Object... args) {
                    if (args.length > 0) {
                        Object value = args[0];
                        callback.onMessage(event, String.valueOf(value));
                    }
                    callback.onMessage(event, (String) null);
                }
            });
        }
    }
}
