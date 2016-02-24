package io.techery.janet;

public class ActionAdapterWrapper extends ActionAdapter implements CallbackWrapper.Interceptor {

    private final ActionAdapter actionAdapter;

    public ActionAdapterWrapper(ActionAdapter actionAdapter) {
        this.actionAdapter = actionAdapter;
    }

    public <A> A onInterceptSend(A action) {
        //do nothing
        return action;
    }

    public <A> A onInterceptCancel(A action) {
        //do nothing
        return action;
    }

    @Override public <A> A onInterceptStart(A action) {
        //do nothing
        return action;
    }

    @Override public <A> A onInterceptProgress(A action, int progress) {
        //do nothing
        return action;
    }

    @Override public <A> A onInterceptSuccess(A action) {
        //do nothing
        return action;
    }

    @Override public <A> A onInterceptFail(A action, JanetException e) {
        //do nothing
        return action;
    }

    @Override <A> void sendInternal(A action) throws JanetException {
        actionAdapter.sendInternal(onInterceptSend(action));
    }

    @Override <A> void cancel(A action) {
        actionAdapter.cancel(onInterceptCancel(action));
    }

    @Override Class getSupportedAnnotationType() {
        return actionAdapter.getSupportedAnnotationType();
    }

    @Override void setCallback(Callback callback) {
        actionAdapter.setCallback(new CallbackWrapper(callback, this));
    }


}
