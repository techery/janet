package io.techery.janet;

import java.io.IOException;

import rx.functions.Action1;

public abstract class ActionAdapter {

    abstract <T> void send(T action, Action1<T> callback) throws IOException;

    abstract Class getActionAnnotationClass();
}
