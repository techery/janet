package io.techery.janet.sample;

import io.techery.janet.ActionAdapter;
import io.techery.janet.ActionAdapterWrapper;
import io.techery.janet.ActionHolder;
import io.techery.janet.JanetException;

public class SampleLoggingAdapter extends ActionAdapterWrapper {

    public SampleLoggingAdapter(ActionAdapter actionAdapter) {
        super(actionAdapter);
    }

    @Override protected <A> ActionHolder<A> onInterceptSend(ActionHolder<A> holder) {
        System.out.println("send " + holder);
        return holder;
    }

    @Override protected <A> A onInterceptCancel(A action) {
        System.out.println("cancel " + action);
        return action;
    }

    @Override protected <A> ActionHolder<A> onInterceptStart(ActionHolder<A> holder) {
        System.out.println("onStart " + holder.action());
        return holder;
    }

    @Override protected <A> ActionHolder<A> onInterceptProgress(ActionHolder<A> holder, int progress) {
        System.out.println("onProgress " + holder.action() + ", progress " + progress);
        return holder;
    }

    @Override protected <A> ActionHolder<A> onInterceptSuccess(ActionHolder<A> holder) {
        System.out.println("onSuccess " + holder.action());
        return holder;
    }

    @Override protected <A> ActionHolder<A> onInterceptFail(ActionHolder<A> holder, JanetException e) {
        System.out.println("onFail " + holder.action());
        return holder;
    }
}
