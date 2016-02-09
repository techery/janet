package io.techery.janet.android;

import android.net.http.AndroidHttpClient;

public final class AndroidApacheClient extends ApacheClient {
    public AndroidApacheClient() {
        super(AndroidHttpClient.newInstance("Janet"));
    }
}
