package io.techery.janet.http.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.techery.janet.body.ActionBody;


public final class Request {

    private final String method;
    private final String url;
    private final List<Header> headers;
    private final ActionBody body;
    /**
     * Some object to mark a request.
     * For example, it'is using for cancellation in OkClient, ApacheClient, UrlConnectionClient
     */
    public Object tag;

    public Request(String method, String url, List<Header> headers, ActionBody body) {
        if (method == null) {
            throw new NullPointerException("Method must not be null.");
        }
        if (url == null) {
            throw new NullPointerException("URL must not be null.");
        }
        this.method = method;
        this.url = url;

        if (headers == null) {
            this.headers = Collections.emptyList();
        } else {
            this.headers = Collections.unmodifiableList(new ArrayList<Header>(headers));
        }

        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public ActionBody getBody() {
        return body;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Request request = (Request) o;

        if (method != null ? !method.equals(request.method) : request.method != null) return false;
        if (url != null ? !url.equals(request.url) : request.url != null) return false;
        if (headers != null ? !headers.equals(request.headers) : request.headers != null) return false;
        return body != null ? body.equals(request.body) : request.body == null;

    }

    @Override public int hashCode() {
        int result = method != null ? method.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Request{" +
                "body=" + body +
                ", headers=" + headers +
                ", url='" + url + '\'' +
                ", method='" + method + '\'' +
                '}';
    }
}
