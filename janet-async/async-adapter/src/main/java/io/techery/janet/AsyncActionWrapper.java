package io.techery.janet;

import java.util.concurrent.ScheduledFuture;

import io.techery.janet.body.ActionBody;
import io.techery.janet.body.BytesArrayBody;
import io.techery.janet.converter.Converter;
import io.techery.janet.converter.ConverterException;

public abstract class AsyncActionWrapper<A> {

    protected final A action;
    private ScheduledFuture scheduledFuture;

    protected AsyncActionWrapper(A action) {this.action = action;}

    protected abstract boolean isBytesMessage();
    protected abstract String getEvent();
    protected abstract ActionBody getMessage(Converter converter) throws ConverterException;
    protected abstract String getResponseEvent();
    protected abstract boolean fillResponse(Object responseAction);
    protected abstract void fillMessage(BytesArrayBody body, Converter converter) throws ConverterException;

    public void cancelExpireFuture() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
    }

    public void setExpireFuture(ScheduledFuture scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    protected long getResponseTimeout() {
        return AsyncActionSynchronizer.PENDING_TIMEOUT;
    }
}