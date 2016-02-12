package io.techery.janet;

import java.util.concurrent.ConcurrentLinkedQueue;

import io.techery.janet.async.actions.ConnectAsyncAction;
import io.techery.janet.async.actions.DisconnectAsyncAction;
import io.techery.janet.async.actions.ErrorAsyncAction;
import io.techery.janet.async.annotations.AsyncAction;
import io.techery.janet.body.ActionBody;
import io.techery.janet.body.BytesArrayBody;
import io.techery.janet.body.StringBody;
import io.techery.janet.converter.Converter;

final public class AsyncActionAdapter extends ActionAdapter {

    static final String FACTORY_CLASS_NAME = "AsyncActionWrapperFactoryImpl";
    static final String ROOSTER_CLASS_NAME = "AsyncActionsRoster";

    private final String url;
    private final AsyncClient client;
    private final Converter converter;

    private final ConcurrentLinkedQueue<ConnectAsyncAction> connectActionQueue;
    private final ConcurrentLinkedQueue<DisconnectAsyncAction> disconnectActionQueue;
    private AsyncActionsRosterBase actionsRoster;
    private final AsyncActionSynchronizer synchronizer;
    private AsyncActionWrapperFactory actionWrapperFactory;


    public AsyncActionAdapter(String url, AsyncClient client, Converter converter) {
        this.url = url;
        this.client = client;
        this.converter = converter;
        this.connectActionQueue = new ConcurrentLinkedQueue<ConnectAsyncAction>();
        this.disconnectActionQueue = new ConcurrentLinkedQueue<DisconnectAsyncAction>();
        this.synchronizer = new AsyncActionSynchronizer();
        loadActionWrapperFactory();
        loadAsyncActionRooster();
        client.setCallback(clientCallback);
    }

    @Override Class getSupportedAnnotationType() {
        return AsyncAction.class;
    }

    @Override protected <T> void sendInternal(T action) throws Throwable {
        callback.onStart(action);
        if (!client.isConnected()
                && action instanceof ConnectAsyncAction) {
            client.connect(url);
            connectActionQueue.add((ConnectAsyncAction) action);
            return;
        }
        if (action instanceof DisconnectAsyncAction) {
            client.disconnect();
            disconnectActionQueue.add((DisconnectAsyncAction) action);
            return;
        }

        AsyncActionWrapper wrapper = actionWrapperFactory.make(action.getClass(), action);
        if (wrapper == null) {
            throw new JanetInternalException("Something was happened with code generator. Check dependence of janet-async-compiler");
        }
        String responseEvent = wrapper.getResponseEvent();
        if (responseEvent != null) {
            synchronizer.put(responseEvent, wrapper);
        }
        if (wrapper.isMessageBytes()) {
            client.send(wrapper.getEvent(), wrapper.getBytesMessage());
        } else {
            ActionBody actionBody = wrapper.getMessage(converter);
            client.send(wrapper.getEvent(), new String(actionBody.getContent()));
        }
    }

    private void onMessageReceived(String event, ActionBody body) {
        if (!actionsRoster.containsEvent(event)) {
            System.err.println(String.format("Received sync message %s is not defined by any action :(. The message contains body %s", event, body));
            return;
        }
        Class actionClass = actionsRoster.getActionClass(event);
        try {
            Object action = actionClass.newInstance();
            AsyncActionWrapper messageWrapper = actionWrapperFactory.make(actionClass, action);
            messageWrapper.fillMessage(body, converter);
            if (synchronizer.contains(event)) {
                for (AsyncActionWrapper wrapper : synchronizer.poll(event)) {
                    if (wrapper.fillResponse(messageWrapper.action)) {
                        callback.onSuccess(wrapper.action);
                    }
                }
            } else {
                callback.onSuccess(action);
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private final AsyncClient.Callback clientCallback = new AsyncClient.Callback() {
        @Override public void onConnect() {
            do {
                ConnectAsyncAction action = connectActionQueue.poll();
                if (action != null) {
                    callback.onSuccess(action);
                } else {
                    callback.onSuccess(new ConnectAsyncAction());
                }
            } while (connectActionQueue.peek() != null);
        }

        @Override public void onDisconnect(String reason) {
            do {
                DisconnectAsyncAction action = disconnectActionQueue.poll();
                if (action != null) {
                    callback.onSuccess(action);
                } else {
                    callback.onSuccess(new DisconnectAsyncAction());
                }
            } while (disconnectActionQueue.peek() != null);
        }

        @Override public void onError(Throwable throwable) {
            callback.onServerError(new ErrorAsyncAction(throwable));
        }

        @Override public void onMessage(String event, String string) {
            onMessageReceived(event, new StringBody(string));
        }

        @Override public void onMessage(String event, byte[] bytes) {
            onMessageReceived(event, new BytesArrayBody(null, bytes));
        }
    };

    @SuppressWarnings("unchecked")
    private void loadActionWrapperFactory() {
        try {
            Class<? extends AsyncActionWrapperFactory> clazz
                    = (Class<? extends AsyncActionWrapperFactory>) Class.forName(FACTORY_CLASS_NAME);
            actionWrapperFactory = clazz.newInstance();
        } catch (Exception e) {
            throw new JanetInternalException("Something was happened with code generator. Check dependence of janet-async-compiler");
        }
    }

    @SuppressWarnings("unchecked")
    private void loadAsyncActionRooster() {
        try {
            Class<? extends AsyncActionsRosterBase> clazz
                    = (Class<? extends AsyncActionsRosterBase>) Class.forName(ROOSTER_CLASS_NAME);
            actionsRoster = clazz.newInstance();
        } catch (Exception e) {
            throw new JanetInternalException("Something was happened with code generator. Check dependence of janet-async-compiler");
        }
    }

    interface AsyncActionWrapperFactory {
        <A> AsyncActionWrapper<A> make(Class<A> actionClass, Object action);
    }

}
