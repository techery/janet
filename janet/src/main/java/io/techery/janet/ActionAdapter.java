package io.techery.janet;

public abstract class ActionAdapter {

    abstract <T> void send(T action);

    abstract void setOnResponseCallback(Callback callback);

    abstract Class getActionAnnotationClass();

    interface Callback {
        void onStart(Object action);
        void onSuccess(Object action);
        void onServerError(Object action);
        void onFail(Object action, Throwable throwable);
    }
}
