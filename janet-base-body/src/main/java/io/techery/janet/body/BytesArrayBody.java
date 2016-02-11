package io.techery.janet.body;

import java.io.IOException;

public class BytesArrayBody extends ActionBody {

    private final byte[] bytes;

    public BytesArrayBody(String mimetype, byte[] bytes) {
        super(mimetype);
        if(bytes == null){
            throw new NullPointerException("bytes == null");
        }
        this.bytes = bytes;
    }

    @Override
    public byte[] getContent() throws IOException {
        return bytes;
    }
}
