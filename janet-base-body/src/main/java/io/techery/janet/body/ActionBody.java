package io.techery.janet.body;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import io.techery.janet.body.util.MimeUtil;

public abstract class ActionBody {

    private final String mimeType;
    private byte[] bytes;

    public ActionBody(String mimeType) {
        if (mimeType == null) {
            mimeType = "application/unknown";
        }
        this.mimeType = mimeType;
    }

    public abstract byte[] getContent() throws IOException;

    private byte[] bytes() {
        if (bytes == null) {
            try {
                bytes = getContent();
                if (bytes == null) {
                    throw new NullPointerException("bytes");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (bytes == null) bytes = new byte[0];
        }
        return bytes;
    }

    public String fileName() {
        return null;
    }

    public String mimeType() {
        return mimeType;
    }

    public InputStream in() throws IOException {
        return new ByteArrayInputStream(bytes());
    }

    public long length() {
        return bytes().length;
    }

    public void writeTo(OutputStream out) throws IOException {
        out.write(bytes());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActionBody that = (ActionBody) o;

        if (!Arrays.equals(bytes(), that.bytes())) return false;
        if (!mimeType.equals(that.mimeType)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mimeType.hashCode();
        result = 31 * result + Arrays.hashCode(bytes());
        return result;
    }

    @Override
    public String toString() {
        String bodyCharset = MimeUtil.parseCharset(mimeType);
        try {
            return new String(bytes(), bodyCharset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "ByteArrayBody[length=" + length() + "]";
    }

}