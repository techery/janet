package io.techery.janet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.techery.janet.body.ActionBody;
import io.techery.janet.converter.Converter;
import io.techery.janet.http.annotations.HttpAction;
import io.techery.janet.http.model.FormUrlEncodedRequestBody;
import io.techery.janet.http.model.Header;
import io.techery.janet.http.model.MultipartRequestBody;
import io.techery.janet.http.model.Request;

public final class RequestBuilder {

    private final Converter converter;
    private String url;

    private FormUrlEncodedRequestBody formBody;
    private MultipartRequestBody multipartBody;
    private ActionBody body;

    private StringBuilder queryParams;
    private List<Header> headers;
    private String contentTypeHeader;
    private HttpAction.Method requestMethod = HttpAction.Method.GET;
    private String ref;

    RequestBuilder(String url, Converter converter) {
        this.url = url;
        this.converter = converter;
        this.multipartBody = null;
        this.formBody = null;
    }

    public void setRequestType(HttpAction.Type type) {
        switch (type) {
            case FORM_URL_ENCODED:
                formBody = new FormUrlEncodedRequestBody();
                multipartBody = null;
                body = formBody;
                break;
            case MULTIPART:
                formBody = null;
                multipartBody = new MultipartRequestBody();
                body = multipartBody;
                break;
            case SIMPLE:
                formBody = null;
                multipartBody = null;
                break;
            default:
                throw new IllegalArgumentException("Unknown request type: " + type);
        }
    }

    public void setUrl(Object param) {
        if (param == null) {
            throw new IllegalArgumentException("@Url field is null.");
        }
        String value = param.toString();
        URL url = null;
        try {
            url = new URL(value);
        } catch (MalformedURLException ignored) {}
        if (url != null) {
            String s = url.toString();
            this.url = s;
            if (url.getQuery() != null) {
                String query = "?" + url.getQuery();
                this.url = s.substring(0, s.indexOf(query));
                this.queryParams = new StringBuilder(query);
            }
            this.ref = url.getRef();
        } else {
            setPath(value);
        }
    }

    public void addHeader(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("Header name must not be null.");
        }
        if ("Content-Type".equalsIgnoreCase(name)) {
            contentTypeHeader = value;
            return;
        }

        List<Header> headers = this.headers;
        if (headers == null) {
            this.headers = headers = new ArrayList<Header>(2);
        }
        headers.add(new Header(name, value));
    }

    public void setPath(String path) {
        StringBuilder url = new StringBuilder(this.url);
        if (this.url.endsWith("/")) {
            url.deleteCharAt(url.length() - 1);
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        this.url = url.append("/").append(path).toString();
    }

    public void addPathParam(String name, String value) {
        addPathParam(name, value, true);
    }

    public void addPathParam(String name, String value, boolean urlEncodeValue) {
        if (name == null) {
            throw new IllegalArgumentException("Path replacement name must not be null.");
        }
        if (value == null) {
            throw new IllegalArgumentException(
                    "Path replacement \"" + name + "\" value must not be null.");
        }
        try {
            if (urlEncodeValue) {
                String encodedValue = URLEncoder.encode(String.valueOf(value), "UTF-8");
                // URLEncoder encodes for use as a query parameter. Path encoding uses %20 to
                // encode spaces rather than +. Query encoding difference specified in HTML spec.
                // Any remaining plus signs represent spaces as already URLEncoded.
                encodedValue = encodedValue.replace("+", "%20");
                url = url.replace("{" + name + "}", encodedValue);
            } else {
                url = url.replace("{" + name + "}", String.valueOf(value));
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(
                    "Unable to convert path parameter \"" + name + "\" value to UTF-8:" + value, e);
        }
    }

    public void addQueryParam(String name, Object value, boolean encodeName, boolean encodeValue) {
        if (value instanceof Iterable) {
            for (Object iterableValue : (Iterable<?>) value) {
                if (iterableValue != null) { // Skip null values
                    addQueryParam(name, iterableValue.toString(), encodeName, encodeValue);
                }
            }
        } else if (value.getClass().isArray()) {
            for (int x = 0, arrayLength = Array.getLength(value); x < arrayLength; x++) {
                Object arrayValue = Array.get(value, x);
                if (arrayValue != null) { // Skip null values
                    addQueryParam(name, arrayValue.toString(), encodeName, encodeValue);
                }
            }
        } else {
            addQueryParam(name, value.toString(), encodeName, encodeValue);
        }
    }

    private void addQueryParam(String name, String value, boolean encodeName, boolean encodeValue) {
        if (name == null) {
            throw new IllegalArgumentException("Query param name must not be null.");
        }
        if (value == null) {
            throw new IllegalArgumentException("Query param \"" + name + "\" value must not be null.");
        }
        try {
            StringBuilder queryParams = this.queryParams;
            if (queryParams == null) {
                this.queryParams = queryParams = new StringBuilder();
            }

            queryParams.append(queryParams.length() > 0 ? '&' : '?');

            if (encodeName) {
                name = URLEncoder.encode(name, "UTF-8");
            }
            if (encodeValue) {
                value = URLEncoder.encode(value, "UTF-8");
            }
            queryParams.append(name).append('=').append(value);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(
                    "Unable to convert query parameter \"" + name + "\" value to UTF-8: " + value, e);
        }
    }

    public void addField(String name, Object value) {
        if (value != null) { // Skip null values.
            if (value instanceof Iterable) {
                for (Object iterableValue : (Iterable<?>) value) {
                    if (iterableValue != null) { // Skip null values.
                        formBody.addField(name, iterableValue.toString());
                    }
                }
            } else if (value.getClass().isArray()) {
                for (int x = 0, arrayLength = Array.getLength(value); x < arrayLength; x++) {
                    Object arrayValue = Array.get(value, x);
                    if (arrayValue != null) { // Skip null values.
                        formBody.addField(name, arrayValue.toString());
                    }
                }
            } else {
                formBody.addField(name, value.toString());
            }
        }
    }

    public void addPart(String name, ActionBody body, String transferEncoding) {
        multipartBody.addPart(name, transferEncoding, body);
    }

    public void setBody(Object value) {
        if (multipartBody != null || formBody != null) {
            throw new IllegalArgumentException("Request is not simple type");
        }
        if (value == null) {
            throw new IllegalArgumentException("Body parameter value must not be null.");
        }
        if (value instanceof ActionBody) {
            body = (ActionBody) value;
        } else {
            body = converter.toBody(value);
        }
    }

    public void setMethod(HttpAction.Method method) {
        this.requestMethod = method;
    }

    Request build() {
        if (multipartBody != null && multipartBody.getPartCount() == 0) {
            throw new IllegalStateException("Multipart requests must contain at least one part.");
        }
        StringBuilder url = new StringBuilder(this.url);
        StringBuilder queryParams = this.queryParams;
        if (queryParams != null) {
            url.append(queryParams);
        }
        if (ref != null) {
            url.append("#");
            url.append(ref);
        }
        ActionBody body = this.body;
        List<Header> headers = this.headers;
        if (contentTypeHeader != null) {
            if (body != null) {
                body = new MimeOverridingTypedOutput(body, contentTypeHeader);
            } else {
                Header header = new Header("Content-Type", contentTypeHeader);
                if (headers == null) {
                    headers = Collections.singletonList(header);
                } else {
                    headers.add(header);
                }
            }
        }
        return new Request(requestMethod.name(), url.toString(), headers, body);
    }

    private static class MimeOverridingTypedOutput extends ActionBody {
        private final ActionBody delegate;
        private final String mimeType;

        MimeOverridingTypedOutput(ActionBody delegate, String mimeType) {
            super(mimeType);
            this.delegate = delegate;
            this.mimeType = mimeType;
        }

        @Override
        public byte[] getContent() throws IOException {
            return delegate.getContent();
        }

        @Override
        public String fileName() {
            return delegate.fileName();
        }

        @Override
        public String mimeType() {
            return mimeType;
        }

        @Override
        public long length() {
            return delegate.length();
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
            delegate.writeTo(out);
        }
    }
}
