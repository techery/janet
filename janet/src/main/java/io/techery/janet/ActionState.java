package io.techery.janet;

/**
 * Use to get action state
 *
 * @param <A>
 */
public final class ActionState<A> {

    /** Action status **/
    public enum Status {
        /**
         * Action is just started to send
         **/
        START,
        /**
         * Sending progress.
         * Get the percentage of progress from {@link ActionState#progress}
         **/
        PROGRESS,
        /**
         * Action is finished without errors
         * Get result from {@link ActionState#action}
         **/
        SUCCESS,
        /**
         * Action is fault.
         * See {@link ActionState#exception}
         **/
        FAIL
    }

    public final A action;
    public final Status status;
    public JanetException exception;
    public int progress;

    private ActionState(A action, Status status) {
        this.action = action;
        this.status = status;
    }

    static <A> ActionState<A> start(A action) {
        return new ActionState<A>(action, Status.START);
    }

    static <A> ActionState<A> progress(A action, int progress) {
        return new ActionState<A>(action, Status.PROGRESS).progress(progress);
    }

    static <A> ActionState<A> success(A action) {
        return new ActionState<A>(action, Status.SUCCESS);
    }

    static <A> ActionState<A> fail(A action, JanetException e) {
        return new ActionState<A>(action, Status.FAIL).exception(e);
    }

    private ActionState<A> exception(JanetException throwable) {
        this.exception = throwable;
        return this;
    }

    private ActionState<A> progress(int progress) {
        this.progress = progress;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActionState<?> that = (ActionState<?>) o;

        if (action != null ? !action.equals(that.action) : that.action != null) return false;
        if (exception != null ? !exception.equals(that.exception) : that.exception != null) return false;
        return status == that.status;

    }

    @Override
    public int hashCode() {
        int result = action != null ? action.hashCode() : 0;
        result = 31 * result + (exception != null ? exception.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ActionState{" +
                "action=" + action +
                ", exception=" + exception +
                ", status=" + status +
                '}';
    }
}
