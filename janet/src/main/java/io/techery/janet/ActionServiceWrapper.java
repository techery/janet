package io.techery.janet;

/**
 * Wrapper for interception lifecycle of delegated {@link ActionService}
 */
public abstract class ActionServiceWrapper extends ActionService {

    private final ActionService actionService;

    public ActionServiceWrapper(ActionService actionService) {
        this.actionService = actionService;
    }

    protected abstract <A> void onInterceptSend(ActionHolder<A> holder);

    protected abstract <A> void onInterceptCancel(ActionHolder<A> holder);

    protected abstract <A> void onInterceptStart(ActionHolder<A> holder);

    protected abstract <A> void onInterceptProgress(ActionHolder<A> holder, int progress);

    protected abstract <A> void onInterceptSuccess(ActionHolder<A> holder);

    protected abstract <A> void onInterceptFail(ActionHolder<A> holder, JanetException e);

    @Override protected <A> void sendInternal(ActionHolder<A> holder) throws JanetException {
        onInterceptSend(holder);
        actionService.sendInternal(holder);
    }

    @Override protected <A> void cancel(ActionHolder<A> holder) {
        onInterceptCancel(holder);
        actionService.cancel(holder);
    }

    @Override protected Class getSupportedAnnotationType() {
        return actionService.getSupportedAnnotationType();
    }

    @Override void setCallback(Callback callback) {
        actionService.setCallback(new CallbackWrapper(callback, interceptor));
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
