package io.techery.janet.async.exception;

import java.util.Locale;

public final class SyncedResponseException extends Throwable {

    private SyncedResponseException(String message) {
        super(message);
    }

    public static SyncedResponseException forTimeout(long timeout) {
        return new SyncedResponseException(String.format(Locale.getDefault(), "Timeout has expired (%d)", timeout));
    }

    public static SyncedResponseException forLimit(int limit) {
        return new SyncedResponseException(String.format(Locale.getDefault(), "Too much actions were sent. More then defined limit (%d)", limit));
    }
}
