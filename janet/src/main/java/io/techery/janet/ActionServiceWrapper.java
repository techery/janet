package io.techery.janet;

/**
 * Wrapper for interception lifecycle of delegated {@link ActionService}
 */
public abstract class ActionServiceWrapper extends ActionService {

    private final ActionService actionService;

    public ActionServiceWrapper(ActionService actionService) {
        this.actionService = actionService;
    }

    /**
     * Called before action sending
     *
     * @param holder action holder for intercepting
     * @return if {@code true} action is finished with status {@linkplain ActionState.Status#SUCCESS SUCCESS} and won't be processed by decorated service
     * @throws JanetException to finish action with status {@linkplain ActionState.Status#FAIL FAIL}
     */
    protected abstract <A> boolean onInterceptSend(ActionHolder<A> holder) throws JanetException;

    /**
     * Called before action cancellation
     *
     * @param holder action holder for intercepting
     */
    protected abstract <A> void onInterceptCancel(ActionHolder<A> holder);

    /**
     * Called from service callback before changing action status to {@linkplain ActionState.Status#START START}
     *
     *
     * @param holder action holder for intercepting
     */
    protected abstract <A> void onInterceptStart(ActionHolder<A> holder);

    /**
     * Called from service callback before changing action status to {@linkplain ActionState.Status#PROGRESS PROGRESS}
     *
     * @param holder action holder for intercepting
     */
    protected abstract <A> void onInterceptProgress(ActionHolder<A> holder, int progress);

    /**
     * Called from service callback before changing action status to {@linkplain ActionState.Status#SUCCESS SUCCESS}
     *
     * @param holder action holder for intercepting
     */
    protected abstract <A> void onInterceptSuccess(ActionHolder<A> holder);

    /**
     * Called from service callback before changing action status to {@linkplain ActionState.Status#FAIL FAIL}
     *
     * @param holder action holder for intercepting
     * @return if {@code true} action will be sent again. Should be careful with it because there is possibility to create an infinite loop of action sending
     */
    protected abstract <A> boolean onInterceptFail(ActionHolder<A> holder, JanetException e);

    /**
     * {@inheritDoc}
     */
    @Override protected <A> void sendInternal(ActionHolder<A> holder) throws JanetException {
        if (onInterceptSend(holder)) callback.onSuccess(holder);
        else actionService.send(holder);
    }

    /**
     * {@inheritDoc}
     */
    @Override protected <A> void cancel(ActionHolder<A> holder) {
        onInterceptCancel(holder);
        actionService.cancel(holder);
    }

    /**
     * {@inheritDoc}
     */
    @Override protected Class getSupportedAnnotationType() {
        return actionService.getSupportedAnnotationType();
    }

    /**
     * {@inheritDoc}
     */
    @Override void setCallback(Callback callback) {
        callback = new CallbackWrapper(callback, interceptor);
        super.setCallback(callback);
        actionService.setCallback(callback);
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

        @Override public <A> boolean interceptFail(ActionHolder<A> holder, JanetException e) {
            boolean resend = onInterceptFail(holder, e);
            if (resend) actionService.send(holder);
            return resend;
        }
    };
}
