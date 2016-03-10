package io.techery.janet.android;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import io.techery.janet.body.ActionBody;
import io.techery.janet.body.BytesArrayBody;
import io.techery.janet.http.internal.ProgressOutputStream;
import io.techery.janet.http.model.Header;
import io.techery.janet.http.model.Request;
import io.techery.janet.http.model.Response;
import io.techery.janet.http.utils.RequestUtils;


public class ApacheClient implements io.techery.janet.http.HttpClient {

    private static HttpClient createDefaultClient() {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, CONNECT_TIMEOUT_MILLIS);
        HttpConnectionParams.setSoTimeout(params, READ_TIMEOUT_MILLIS);
        return new DefaultHttpClient(params);
    }

    private final HttpClient client;

    public ApacheClient() {
        this(createDefaultClient());
    }

    public ApacheClient(HttpClient client) {
        this.client = client;
    }

    @Override public Response execute(Request request, RequestCallback requestCallback) throws IOException {
        HttpUriRequest apacheRequest = createRequest(request, requestCallback);
        RequestUtils.throwIfCanceled(request);
        request.tag = apacheRequest; //mark for cancellation
        HttpResponse apacheResponse = execute(client, apacheRequest);
        RequestUtils.throwIfCanceled(request);
        return parseResponse(request.getUrl(), apacheResponse);
    }

    @Override public void cancel(Request request) {
        if (request.tag != null && (request.tag instanceof HttpUriRequest)) {
            HttpUriRequest apacheRequest = (HttpUriRequest) request.tag;
            apacheRequest.abort();
        }
        request.tag = RequestUtils.TAG_CANCELED;
    }

    protected HttpResponse execute(HttpClient client, HttpUriRequest request) throws IOException {
        return client.execute(request);
    }

    static HttpUriRequest createRequest(Request request, RequestCallback requestCallback) {
        if (request.getBody() != null) {
            return new GenericEntityHttpRequest(request, requestCallback);
        }
        return new GenericHttpRequest(request);
    }

    static Response parseResponse(String url, HttpResponse response) throws IOException {
        StatusLine statusLine = response.getStatusLine();
        int status = statusLine.getStatusCode();
        String reason = statusLine.getReasonPhrase();

        List<Header> headers = new ArrayList<Header>();
        String contentType = "application/octet-stream";
        for (org.apache.http.Header header : response.getAllHeaders()) {
            String name = header.getName();
            String value = header.getValue();
            if ("Content-Type".equalsIgnoreCase(name)) {
                contentType = value;
            }
            headers.add(new Header(name, value));
        }

        BytesArrayBody body = null;
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            byte[] bytes = EntityUtils.toByteArray(entity);
            body = new BytesArrayBody(contentType, bytes);
        }

        return new Response(url, status, reason, headers, body);
    }

    private static class GenericHttpRequest extends HttpRequestBase {
        private final String method;

        public GenericHttpRequest(Request request) {
            method = request.getMethod();
            setURI(URI.create(request.getUrl()));

            // Add all headers.
            for (Header header : request.getHeaders()) {
                addHeader(new BasicHeader(header.getName(), header.getValue()));
            }
        }

        @Override
        public String getMethod() {
            return method;
        }
    }

    private static class GenericEntityHttpRequest extends HttpEntityEnclosingRequestBase {
        private final String method;

        GenericEntityHttpRequest(Request request, RequestCallback requestCallback) {
            super();
            method = request.getMethod();
            setURI(URI.create(request.getUrl()));

            // Add all headers.
            for (Header header : request.getHeaders()) {
                addHeader(new BasicHeader(header.getName(), header.getValue()));
            }

            // Add the content body.
            setEntity(new TypedOutputEntity(request.getBody(), requestCallback));
        }

        @Override
        public String getMethod() {
            return method;
        }
    }

    static class TypedOutputEntity extends AbstractHttpEntity {
        private final ActionBody requestBody;
        private final RequestCallback requestCallback;

        TypedOutputEntity(ActionBody requestBody, RequestCallback requestCallback) {
            this.requestBody = requestBody;
            this.requestCallback = requestCallback;
            setContentType(requestBody.mimeType());
        }

        @Override
        public boolean isRepeatable() {
            return true;
        }

        @Override
        public long getContentLength() {
            return requestBody.length();
        }

        @Override
        public InputStream getContent() throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            requestBody.writeTo(out);
            return new ByteArrayInputStream(out.toByteArray());
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
            requestBody.writeTo(new ProgressOutputStream(out, new ProgressOutputStream.ProgressListener() {
                @Override public void onProgressChanged(long bytesWritten) {
                    requestCallback.onProgress((int) ((bytesWritten * 100) / getContentLength()));
                }
            }));
        }

        @Override
        public boolean isStreaming() {
            return false;
        }
    }

}
