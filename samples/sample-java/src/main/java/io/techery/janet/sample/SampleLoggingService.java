package io.techery.janet.sample;

import io.techery.janet.ActionService;
import io.techery.janet.ActionServiceWrapper;
import io.techery.janet.ActionHolder;
import io.techery.janet.JanetException;

public class SampleLoggingService extends ActionServiceWrapper {

    public SampleLoggingService(ActionService actionService) {
        super(actionService);
    }

    @Override protected <A> void onInterceptSend(ActionHolder<A> holder) {
        System.out.println("send " + holder.action());
    }

    @Override protected <A> void onInterceptCancel(ActionHolder<A> holder) {
        System.out.println("cancel " + holder.action());
    }

    @Override protected <A> void onInterceptStart(ActionHolder<A> holder) {
        System.out.println("onStart " + holder.action());
    }

    @Override protected <A> void onInterceptProgress(ActionHolder<A> holder, int progress) {
        System.out.println("onProgress " + holder.action() + ", progress " + progress);
    }

    @Override protected <A> void onInterceptSuccess(ActionHolder<A> holder) {
        System.out.println("onSuccess " + holder.action());
    }

    @Override protected <A> void onInterceptFail(ActionHolder<A> holder, JanetException e) {
        System.out.println("onFail " + holder.action());
    }
}
