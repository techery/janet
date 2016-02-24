package io.techery.janet.sample;

import io.techery.janet.ActionAdapter;
import io.techery.janet.ActionAdapterWrapper;
import io.techery.janet.JanetException;

public class SampleLoggingAdapter extends ActionAdapterWrapper {

    public SampleLoggingAdapter(ActionAdapter actionAdapter) {
        super(actionAdapter);
    }

    @Override protected <A> A onInterceptSend(A action) {
        System.out.println("send " + action);
        return action;
    }

    @Override protected <A> A onInterceptCancel(A action) {
        System.out.println("cancel " + action);
        return action;
    }

    @Override protected <A> A onInterceptStart(A action) {
        System.out.println("onStart " + action);
        return action;
    }

    @Override protected <A> A onInterceptProgress(A action, int progress) {
        System.out.println("onProgress " + action + ", progress " + progress);
        return action;
    }

    @Override protected <A> A onInterceptSuccess(A action) {
        System.out.println("onSuccess " + action);
        return action;
    }

    @Override protected <A> A onInterceptFail(A action, JanetException e) {
        System.out.println("onFail " + action);
        return action;
    }
}
