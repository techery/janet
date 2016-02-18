package io.techery.janet.http;


import java.io.IOException;

import io.techery.janet.http.model.Request;
import io.techery.janet.http.model.Response;

public interface HttpClient {

    int CONNECT_TIMEOUT_MILLIS = 15 * 1000; // 15s
    int READ_TIMEOUT_MILLIS = 20 * 1000; // 20s
    long PROGRESS_THRESHOLD = 1024;

    Response execute(Request request, RequestCallback requestCallback) throws IOException;

    interface RequestCallback {
        void onProgress(int progress);
    }
}
