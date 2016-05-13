package io.techery.janet.util;

import io.techery.janet.ActionHolder;
import io.techery.janet.ActionService;
import io.techery.janet.ActionServiceWrapper;
import io.techery.janet.JanetException;

public class StubServiceWrapper extends ActionServiceWrapper {

    private StubCallback stubCallback;

    public StubServiceWrapper(ActionService actionService) {
        super(actionService);
    }

    public void setStubCallback(StubCallback stubCallback) {
        this.stubCallback = stubCallback;
    }

    public StubCallback getStubCallback() {
        return stubCallback;
    }

    @Override protected <A> boolean onInterceptSend(ActionHolder<A> holder) {
        if (stubCallback != null) {
            stubCallback.onInterceptSend(holder);
        }
        return false;
    }

    @Override protected <A> void onInterceptCancel(ActionHolder<A> holder) {
        if (stubCallback != null) {
            stubCallback.onInterceptCancel(holder);
        }
    }

    @Override protected <A> void onInterceptStart(ActionHolder<A> holder) {
        if (stubCallback != null) {
            stubCallback.onInterceptStart(holder);
        }
    }

    @Override protected <A> void onInterceptProgress(ActionHolder<A> holder, int progress) {
        if (stubCallback != null) {
            stubCallback.onInterceptProgress(holder, progress);
        }
    }

    @Override protected <A> void onInterceptSuccess(ActionHolder<A> holder) {
        if (stubCallback != null) {
            stubCallback.onInterceptSuccess(holder);
        }
    }

    @Override protected <A> boolean onInterceptFail(ActionHolder<A> holder, JanetException e) {
        if (stubCallback != null) {
            stubCallback.onInterceptFail(holder, e);
        }
        return false;
    }

    public interface StubCallback {
        <A> void onInterceptSend(ActionHolder<A> holder);

        <A> void onInterceptCancel(ActionHolder<A> holder);

        <A> void onInterceptStart(ActionHolder<A> holder);

        <A> void onInterceptProgress(ActionHolder<A> holder, int progress);

        <A> void onInterceptSuccess(ActionHolder<A> holder);

        <A> void onInterceptFail(ActionHolder<A> holder, JanetException e);
    }
}
