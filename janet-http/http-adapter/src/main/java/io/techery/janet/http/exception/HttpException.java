package io.techery.janet.http.exception;


public class HttpException extends Exception {
    private final int status;
    private final String reason;

    public HttpException(int status, String reason) {
        super("HTTP " + status + " " + reason);
        this.status = status;
        this.reason = reason;
    }

    /** HTTP status status. */
    public int code() {
        return status;
    }

    /** HTTP status message. */
    public String status() {
        return reason;
    }

}
