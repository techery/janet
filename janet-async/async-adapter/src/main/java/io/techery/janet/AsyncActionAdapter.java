package io.techery.janet;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.techery.janet.AsyncActionAdapter.QueuePoller.PollCallback;
import io.techery.janet.async.actions.ConnectAsyncAction;
import io.techery.janet.async.actions.DisconnectAsyncAction;
import io.techery.janet.async.actions.ErrorAsyncAction;
import io.techery.janet.async.annotations.AsyncAction;
import io.techery.janet.async.exception.AsyncAdapterException;
import io.techery.janet.async.exception.SyncedResponseException;
import io.techery.janet.body.ActionBody;
import io.techery.janet.body.BytesArrayBody;
import io.techery.janet.body.StringBody;
import io.techery.janet.converter.Converter;
import io.techery.janet.converter.ConverterException;

final public class AsyncActionAdapter extends ActionAdapter {

    static final String ROSTER_CLASS_SIMPLE_NAME = "AsyncActionsRoster";
    private final static String ROSTER_CLASS_NAME = Janet.class.getPackage()
            .getName() + "." + ROSTER_CLASS_SIMPLE_NAME;

    static final String FACTORY_CLASS_SIMPLE_NAME = "AsyncActionWrapperFactoryImpl";
    private final static String FACTORY_CLASS_NAME = Janet.class.getPackage()
            .getName() + "." + FACTORY_CLASS_SIMPLE_NAME;

    private static final String ERROR_GENERATOR = "Something was happened with code generator. Check dependence of janet-async-compiler";

    private final String url;
    private final AsyncClient client;
    private final Converter converter;

    private final ConcurrentLinkedQueue<ConnectAsyncAction> connectActionQueue;
    private final ConcurrentLinkedQueue<DisconnectAsyncAction> disconnectActionQueue;
    private AsyncActionsRosterBase actionsRoster;
    private final AsyncActionSynchronizer synchronizer;
    private AsyncActionWrapperFactory actionWrapperFactory;


    public AsyncActionAdapter(String url, AsyncClient client, Converter converter) {
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        if (client == null) {
            throw new IllegalArgumentException("client == null");
        }
        if (converter == null) {
            throw new IllegalArgumentException("converter == null");
        }
        this.url = url;
        this.client = client;
        this.converter = converter;
        this.connectActionQueue = new ConcurrentLinkedQueue<ConnectAsyncAction>();
        this.disconnectActionQueue = new ConcurrentLinkedQueue<DisconnectAsyncAction>();
        this.synchronizer = new AsyncActionSynchronizer(waitingErrorCallback);
        loadActionWrapperFactory();
        loadAsyncActionRooster();
        client.setCallback(clientCallback);
        for (String event : actionsRoster.getRegisteredEvents()) {
            client.subscribe(event);
        }
    }

    @Override Class getSupportedAnnotationType() {
        return AsyncAction.class;
    }

    @Override protected <T> void sendInternal(T action) throws AsyncAdapterException {
        callback.onStart(action);
        if (handleConnectionAction(action)) {
            return;
        }
        AsyncActionWrapper wrapper = actionWrapperFactory.make(action.getClass(), action);
        if (wrapper == null) {
            throw new JanetInternalException(ERROR_GENERATOR);
        }
        if (!client.isConnected()) {
            connect(false);
        }
        sendAction(wrapper);
        callback.onProgress(action, 100);
    }

    private void sendAction(AsyncActionWrapper wrapper) throws AsyncAdapterException {
        String responseEvent = wrapper.getResponseEvent();
        if (responseEvent != null) {
            synchronizer.put(responseEvent, wrapper);
        }
        try {
            ActionBody actionBody = wrapper.getMessage(converter);
            byte[] content = actionBody.getContent();
            if (wrapper.isBytesMessage()) {
                client.send(wrapper.getEvent(), content);
            } else {
                client.send(wrapper.getEvent(), new String(content));
            }
        } catch (Throwable t) {
            throw new AsyncAdapterException(t);
        }
    }

    private <T> boolean handleConnectionAction(T action) throws AsyncAdapterException {
        if (action instanceof ConnectAsyncAction) {
            ConnectAsyncAction connectAsyncAction = (ConnectAsyncAction) action;
            if (client.isConnected() && !connectAsyncAction.reconnectIfConnected) {
                callback.onSuccess(action);
            }
            connect(connectAsyncAction.reconnectIfConnected);
            connectActionQueue.add(connectAsyncAction);
            return true;
        }
        if (action instanceof DisconnectAsyncAction) {
            disconnect();
            disconnectActionQueue.add((DisconnectAsyncAction) action);
            return true;
        }
        return false;
    }

    private void connect(boolean reconnectIfConnected) throws AsyncAdapterException {
        try {
            client.connect(url, reconnectIfConnected);
        } catch (Throwable t) {
            throw new AsyncAdapterException(t);
        }
    }

    private void disconnect() throws AsyncAdapterException {
        try {
            client.disconnect();
        } catch (Throwable t) {
            throw new AsyncAdapterException(t);
        }
    }

    private void onMessageReceived(String event, BytesArrayBody body) {
        if (!actionsRoster.containsEvent(event)) {
            System.err.println(String.format("Received sync message %s is not defined by any action :(. The message contains body %s", event, body));
            return;
        }
        List<Class> actionClassList = actionsRoster.getActionClasses(event);
        for (Class actionClass : actionClassList) {
            Object action = createActionInstance(actionClass);
            AsyncActionWrapper messageWrapper = actionWrapperFactory.make(actionClass, action);
            try {
                messageWrapper.fillMessage(body, converter);
            } catch (ConverterException e) {
                callback.onFail(action, new AsyncAdapterException(e));
            }
            if (synchronizer.contains(event)) {
                for (AsyncActionWrapper wrapper : synchronizer.sync(event, messageWrapper.action, new AsyncActionSynchronizer.Predicate() {
                    @Override public boolean call(AsyncActionWrapper wrapper, Object responseAction) {
                        try {
                            return wrapper.fillResponse(responseAction);
                        } catch (ConverterException e) {
                            callback.onFail(wrapper.action, new AsyncAdapterException(e));
                        }
                        return false;
                    }
                })) {
                    callback.onSuccess(wrapper.action);
                }
            } else {
                callback.onSuccess(action);
            }
        }
    }

    private final AsyncClient.Callback clientCallback = new AsyncClient.Callback() {

        private QueuePoller queuePoller = new QueuePoller();

        @Override public void onConnect() {
            queuePoller.poll(connectActionQueue, new PollCallback<ConnectAsyncAction>() {
                @Override public ConnectAsyncAction createIfEmpty() {
                    return new ConnectAsyncAction();
                }

                @Override public void onNext(ConnectAsyncAction item) {
                    callback.onSuccess(item);
                }
            });
        }

        @Override public void onDisconnect(String reason) {
            queuePoller.poll(disconnectActionQueue, new PollCallback<DisconnectAsyncAction>() {
                @Override public DisconnectAsyncAction createIfEmpty() {
                    return new DisconnectAsyncAction();
                }

                @Override public void onNext(DisconnectAsyncAction item) {
                    callback.onSuccess(item);
                }
            });
        }

        @Override public void onConnectionError(final Throwable t) {
            queuePoller.poll(connectActionQueue, new PollCallback<ConnectAsyncAction>() {
                @Override public ConnectAsyncAction createIfEmpty() {
                    return new ConnectAsyncAction();
                }

                @Override public void onNext(ConnectAsyncAction item) {
                    callback.onFail(item, new AsyncAdapterException("ConnectionError", t));
                }
            });
        }

        @Override public void onError(Throwable t) {
            callback.onFail(new ErrorAsyncAction(t), new AsyncAdapterException("Server sent error", t));
        }

        @Override public void onMessage(String event, String string) {
            BytesArrayBody body = null;
            if (string != null) {
                body = new StringBody(string);
            }
            onMessageReceived(event, body);
        }

        @Override public void onMessage(String event, byte[] bytes) {
            BytesArrayBody body = null;
            if (bytes != null) {
                body = new BytesArrayBody(null, bytes);
            }
            onMessageReceived(event, body);
        }
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final AsyncActionSynchronizer.OnCleanedListener waitingErrorCallback = new AsyncActionSynchronizer.OnCleanedListener() {
        @Override
        public void onCleaned(AsyncActionWrapper wrapper, Reason reason) {
            SyncedResponseException exception = null;
            switch (reason) {
                case TIMEOUT: {
                    exception = SyncedResponseException.forTimeout(wrapper.getDelayMillis());
                    break;
                }
                case LIMIT: {
                    exception = SyncedResponseException.forLimit(AsyncActionSynchronizer.PENDING_ACTIONS_EVENT_LIMIT);
                    break;
                }
            }
            callback.onFail(wrapper.action, new AsyncAdapterException("Action " + wrapper.action + " has not waited.", exception));
        }
    };

    @SuppressWarnings("unchecked")
    private void loadActionWrapperFactory() {
        try {
            Class<? extends AsyncActionWrapperFactory> clazz
                    = (Class<? extends AsyncActionWrapperFactory>) Class.forName(FACTORY_CLASS_NAME);
            actionWrapperFactory = clazz.newInstance();
        } catch (Exception e) {
            throw new JanetInternalException(ERROR_GENERATOR);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadAsyncActionRooster() {
        try {
            Class<? extends AsyncActionsRosterBase> clazz
                    = (Class<? extends AsyncActionsRosterBase>) Class.forName(ROSTER_CLASS_NAME);
            actionsRoster = clazz.newInstance();
        } catch (Exception e) {
            throw new JanetInternalException(ERROR_GENERATOR);
        }
    }

    private Object createActionInstance(Class aClass) {
        try {
            return aClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    interface AsyncActionWrapperFactory {
        <A> AsyncActionWrapper<A> make(Class<A> actionClass, Object action);
    }

    static class QueuePoller {
        <U> void poll(Queue<U> q, PollCallback<U> callback) {
            do {
                U item = q.poll();
                if (item == null) {
                    item = callback.createIfEmpty();
                }
                callback.onNext(item);
            } while (q.peek() != null);
        }

        interface PollCallback<T> {
            void onNext(T item);
            T createIfEmpty();
        }
    }
}
