package io.techery.janet;

public abstract class ActionAdapterWrapper extends ActionAdapter {

    private final ActionAdapter actionAdapter;

    public ActionAdapterWrapper(ActionAdapter actionAdapter) {
        this.actionAdapter = actionAdapter;
    }

    protected abstract <A> ActionHolder<A> onInterceptSend(ActionHolder<A> holder);

    protected abstract <A> A onInterceptCancel(A holder);

    protected abstract <A> ActionHolder<A> onInterceptStart(ActionHolder<A> holder);

    protected abstract <A> ActionHolder<A> onInterceptProgress(ActionHolder<A> holder, int progress);

    protected abstract <A> ActionHolder<A> onInterceptSuccess(ActionHolder<A> holder);

    protected abstract <A> ActionHolder<A> onInterceptFail(ActionHolder<A> holder, JanetException e);

    @Override protected <A> void sendInternal(ActionHolder<A> holder) throws JanetException {
        onInterceptSend(holder);
        actionAdapter.sendInternal(holder);
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
        @Override public <A> ActionHolder<A> interceptStart(ActionHolder<A> holder) {
            return onInterceptStart(holder);
        }

        @Override public <A> ActionHolder<A> interceptProgress(ActionHolder<A> holder, int progress) {
            return onInterceptProgress(holder, progress);
        }

        @Override public <A> ActionHolder<A> interceptSuccess(ActionHolder<A> holder) {
            return onInterceptSuccess(holder);
        }

        @Override public <A> ActionHolder<A> interceptFail(ActionHolder<A> holder, JanetException e) {
            return onInterceptFail(holder, e);
        }
    };
}
