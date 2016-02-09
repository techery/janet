package io.techery.janet.okhttp;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import io.techery.janet.http.UrlConnectionClient;
import io.techery.janet.http.model.Request;

/**
 * Janet client that uses OkHttp.
 */
public class OkClient extends UrlConnectionClient {
    private static OkHttpClient generateDefaultOkHttp() {
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        client.setReadTimeout(READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        return client;
    }

    private final OkUrlFactory okUrlFactory;

    public OkClient() {
        this(generateDefaultOkHttp());
    }

    public OkClient(OkHttpClient client) {
        this.okUrlFactory = new OkUrlFactory(client);
    }

    @Override
    protected HttpURLConnection openConnection(Request request) throws IOException {
        return okUrlFactory.open(new URL(request.getUrl()));
    }
}
