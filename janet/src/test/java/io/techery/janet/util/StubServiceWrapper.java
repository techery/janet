package io.techery.janet.util;

import io.techery.janet.ActionHolder;
import io.techery.janet.ActionService;
import io.techery.janet.ActionServiceWrapper;
import io.techery.janet.JanetException;

public class StubServiceWrapper extends ActionServiceWrapper {

    public StubServiceWrapper(ActionService actionService) {
        super(actionService);
    }

    @Override protected <A> boolean onInterceptSend(ActionHolder<A> holder) {
        return false;
    }

    @Override protected <A> void onInterceptCancel(ActionHolder<A> holder) {

    }

    @Override protected <A> void onInterceptStart(ActionHolder<A> holder) {

    }

    @Override protected <A> void onInterceptProgress(ActionHolder<A> holder, int progress) {

    }

    @Override protected <A> void onInterceptSuccess(ActionHolder<A> holder) {

    }

    @Override protected <A> void onInterceptFail(ActionHolder<A> holder, JanetException e) {

    }
}
