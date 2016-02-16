package io.techery.janet;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import io.techery.janet.body.ActionBody;
import io.techery.janet.body.BytesArrayBody;
import io.techery.janet.converter.Converter;

public abstract class AsyncActionWrapper<A> implements Delayed {

    protected final A action;

    private final long startTime = System.currentTimeMillis();

    protected AsyncActionWrapper(A action) {this.action = action;}

    protected abstract boolean isBytesMessage();
    protected abstract String getEvent();
    protected abstract ActionBody getMessage(Converter converter);
    protected abstract String getResponseEvent();
    protected abstract boolean fillResponse(Object responseAction);
    protected abstract void fillMessage(BytesArrayBody body, Converter converter);

    protected long getResponseTimeout(){
        return AsyncActionSynchronizer.PENDING_TIMEOUT;
    }

    final long getDelayMillis() {
        return (startTime + getResponseTimeout()) - System.currentTimeMillis();
    }

    @Override
    final public long getDelay(TimeUnit unit) {
        return unit.convert(getDelayMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed that) {
        long thisMillis = this.getDelayMillis();
        long thatMillis = ((AsyncActionWrapper) that).getDelayMillis();
        return (thisMillis < thatMillis) ? -1 : ((thisMillis == thatMillis) ? 0 : 1);
    }
}