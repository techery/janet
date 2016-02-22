package io.techery.janet;

public final class ActionState<A> {

    public enum Status {
        START, PROGRESS, SUCCESS, FAIL, SERVER_ERROR
    }

    public final A action;
    public final Status status;
    public Throwable throwable;
    public int progress;

    private ActionState(A action, Status status) {
        this.action = action;
        this.status = status;
    }

    public static <A> ActionState<A> start(A action) {
        return new ActionState<A>(action, Status.START);
    }

    public static <A> ActionState<A> progress(A action, int progress) {
        return new ActionState<A>(action, Status.PROGRESS).progress(progress);
    }

    public static <A> ActionState<A> success(A action) {
        return new ActionState<A>(action, Status.SUCCESS);
    }

    public static <A> ActionState<A> fail(A action, Throwable throwable) {
        return new ActionState<A>(action, Status.FAIL).throwable(throwable);
    }

    public static <A> ActionState<A> error(A action) {
        return new ActionState<A>(action, Status.SERVER_ERROR);
    }

    private ActionState<A> throwable(Throwable throwable) {
        this.throwable = throwable;
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
        if (throwable != null ? !throwable.equals(that.throwable) : that.throwable != null) return false;
        return status == that.status;

    }

    @Override
    public int hashCode() {
        int result = action != null ? action.hashCode() : 0;
        result = 31 * result + (throwable != null ? throwable.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ActionState{" +
                "action=" + action +
                ", throwable=" + throwable +
                ", status=" + status +
                '}';
    }
}
