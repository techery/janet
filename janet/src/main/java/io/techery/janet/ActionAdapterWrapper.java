package io.techery.janet;

/**
 * Wrapper for interception lifecycle of delegated ActionAdapter
 */
public abstract class ActionAdapterWrapper extends ActionAdapter {

    private final ActionAdapter actionAdapter;

    public ActionAdapterWrapper(ActionAdapter actionAdapter) {
        this.actionAdapter = actionAdapter;
    }

    protected abstract <A> void onInterceptSend(ActionHolder<A> holder);

    protected abstract <A> void onInterceptCancel(ActionHolder<A> holder);

    protected abstract <A> void onInterceptStart(ActionHolder<A> holder);

    protected abstract <A> void onInterceptProgress(ActionHolder<A> holder, int progress);

    protected abstract <A> void onInterceptSuccess(ActionHolder<A> holder);

    protected abstract <A> void onInterceptFail(ActionHolder<A> holder, JanetException e);

    @Override protected <A> void sendInternal(ActionHolder<A> holder) throws JanetException {
        onInterceptSend(holder);
        actionAdapter.sendInternal(holder);
    }

    @Override protected <A> void cancel(ActionHolder<A> holder) {
        onInterceptCancel(holder);
        actionAdapter.cancel(holder);
    }

    @Override protected Class getSupportedAnnotationType() {
        return actionAdapter.getSupportedAnnotationType();
    }

    @Override void setCallback(Callback callback) {
        actionAdapter.setCallback(new CallbackWrapper(callback, interceptor));
    }

    private final CallbackWrapper.Interceptor interceptor = new CallbackWrapper.Interceptor() {
        @Override public <A> void interceptStart(ActionHolder<A> holder) {
            onInterceptStart(holder);
        }

        @Override public <A> void interceptProgress(ActionHolder<A> holder, int progress) {
            onInterceptProgress(holder, progress);
        }

        @Override public <A> void interceptSuccess(ActionHolder<A> holder) {
            onInterceptSuccess(holder);
        }

        @Override public <A> void interceptFail(ActionHolder<A> holder, JanetException e) {
            onInterceptFail(holder, e);
        }
    };
}
