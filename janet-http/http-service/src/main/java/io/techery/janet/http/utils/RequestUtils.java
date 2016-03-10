package io.techery.janet.http.utils;

import java.io.IOException;

import io.techery.janet.http.model.Request;

public class RequestUtils {

    public static final Object TAG_CANCELED = new Object();

    public static void throwIfCanceled(Request request) throws IOException {
        if (request.tag == TAG_CANCELED) {
            throw new IOException("Request is canceled");
        }
    }
}
