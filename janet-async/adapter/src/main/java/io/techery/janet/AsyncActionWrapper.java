package io.techery.janet;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import io.techery.janet.body.ActionBody;
import io.techery.janet.converter.Converter;

public abstract class AsyncActionWrapper<A> implements Delayed {

    protected final A action;

    private final long startTime = System.currentTimeMillis();

    protected AsyncActionWrapper(A action) {this.action = action;}

    protected abstract boolean isBytesMessage();
    protected abstract String getEvent();
    protected abstract byte[] getBytesMessage();
    protected abstract ActionBody getMessage(Converter converter);
    protected abstract String getResponseEvent();
    protected abstract boolean fillResponse(Object responseAction);
    protected abstract void fillMessage(ActionBody body, Converter converter);

    final long getDelayMillis() {
        return (startTime + AsyncActionSynchronizer.PENDING_TIMEOUT) - System.currentTimeMillis();
    }

    @Override
    final public long getDelay(TimeUnit unit) {
        return unit.convert(getDelayMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed that) {
        return Long.compare(this.getDelayMillis(), ((AsyncActionWrapper) that).getDelayMillis());
    }
}