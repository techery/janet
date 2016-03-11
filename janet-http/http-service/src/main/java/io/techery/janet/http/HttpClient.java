package io.techery.janet.http;


import java.io.IOException;

import io.techery.janet.http.model.Request;
import io.techery.janet.http.model.Response;

/**
 * Abstraction of an HTTP client which can execute {@linkplain Request Requests}. This class must be
 * thread-safe as invocation may happen from multiple threads simultaneously.
 */
public interface HttpClient {

    int CONNECT_TIMEOUT_MILLIS = 15 * 1000; // 15s
    int READ_TIMEOUT_MILLIS = 20 * 1000; // 20s
    long PROGRESS_THRESHOLD = 1024;

    /**
     * Synchronously execute an HTTP represented by {@code request} and encapsulate all response data
     * into a {@linkplain Response} instance.
     */
    Response execute(Request request, RequestCallback requestCallback) throws IOException;

    /**
     * Immediately cancel running request.
     * <p>
     * Use {@linkplain Request#tag} for setting internal cancellation object on execution before to use it here.
     */
    void cancel(Request request);

    interface RequestCallback {
        void onProgress(int progress);
    }
}
