package io.techery.janet.http.model;

import java.io.IOException;
import java.io.OutputStream;

import io.techery.janet.body.ActionBody;

public class MimeOverridingTypedOutput extends ActionBody {
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