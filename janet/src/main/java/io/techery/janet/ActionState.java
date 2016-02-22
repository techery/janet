package io.techery.janet;

public class ActionState<A> {

    public enum Status {
        START, PROGRESS, SUCCESS, FAIL
    }

    public A action;
    public Throwable throwable;
    public Status status;
    public int progress;

    public ActionState(A action) {
        this.action = action;
    }

    public ActionState<A> action(A action) {
        this.action = action;
        return this;
    }

    public ActionState<A> throwable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    public ActionState<A> status(Status status) {
        this.status = status;
        return this;
    }

    public ActionState<A> progress(int progress) {
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
