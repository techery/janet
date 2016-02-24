package io.techery.janet;

public abstract class ActionAdapterWrapper extends ActionAdapter {

    private final ActionAdapter actionAdapter;

    public ActionAdapterWrapper(ActionAdapter actionAdapter) {
        this.actionAdapter = actionAdapter;
    }

    protected abstract <A> A onInterceptSend(A action);

    protected abstract <A> A onInterceptCancel(A action);

    protected abstract <A> A onInterceptStart(A action);

    protected abstract <A> A onInterceptProgress(A action, int progress);

    protected abstract <A> A onInterceptSuccess(A action);

    protected abstract <A> A onInterceptFail(A action, JanetException e);

    @Override protected <A> void sendInternal(A action) throws JanetException {
        actionAdapter.sendInternal(onInterceptSend(action));
    }

    @Override protected <A> void cancel(A action) {
        actionAdapter.cancel(onInterceptCancel(action));
    }

    @Override protected Class getSupportedAnnotationType() {
        return actionAdapter.getSupportedAnnotationType();
    }

    @Override void setCallback(Callback callback) {
        actionAdapter.setCallback(new CallbackWrapper(callback, interceptor));
    }

    private final CallbackWrapper.Interceptor interceptor = new CallbackWrapper.Interceptor() {
        @Override public <A> A interceptStart(A action) {
            return onInterceptStart(action);
        }

        @Override public <A> A interceptProgress(A action, int progress) {
            return onInterceptProgress(action, progress);
        }

        @Override public <A> A interceptSuccess(A action) {
            return onInterceptSuccess(action);
        }

        @Override public <A> A interceptFail(A action, JanetException e) {
            return onInterceptFail(action, e);
        }
    };
}
