package io.techery.janet.body;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class StringBody extends BytesArrayBody {

    private final String string;

    public StringBody(String string) {
        super("text/plain; charset=UTF-8", convertToBytes(string));
        this.string = string;
    }

    private static byte[] convertToBytes(String string) {
        try {
            return string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getString() {
        return string;
    }

    @Override
    public String toString() {
        try {
            return "StringBody[" + new String(getContent(), "UTF-8") + "]";
        } catch (IOException e) {
            throw new AssertionError("Must be able to decode UTF-8");
        }
    }
}
