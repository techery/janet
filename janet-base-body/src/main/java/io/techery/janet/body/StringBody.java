package io.techery.janet.body;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class StringBody extends ActionBody {

    private final String string;

    public StringBody(String string) {
        super("text/plain; charset=UTF-8");
        this.string = string;
    }

    @Override
    public byte[] getContent() throws IOException {
        return convertToBytes(string);
    }

    private static byte[] convertToBytes(String string) {
        try {
            return string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
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
