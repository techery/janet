package io.techery.janet.http.exception;


import io.techery.janet.http.model.Response;

public class HttpException extends Exception {
    private final Response response;

    public HttpException(Response response) {
        super("HTTP " + response.getStatus() + " " + response.getReason());
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }
}
