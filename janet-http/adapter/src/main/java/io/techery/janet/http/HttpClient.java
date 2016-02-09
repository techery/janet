package io.techery.janet.http;


import java.io.IOException;

import io.techery.janet.http.model.Request;
import io.techery.janet.http.model.Response;

public interface HttpClient {

    int CONNECT_TIMEOUT_MILLIS = 15 * 1000; // 15s
    int READ_TIMEOUT_MILLIS = 20 * 1000; // 20s

    Response execute(Request request) throws IOException;
}
