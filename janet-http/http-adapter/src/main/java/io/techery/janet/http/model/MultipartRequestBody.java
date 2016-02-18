package io.techery.janet.http.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import io.techery.janet.body.ActionBody;

public final class MultipartRequestBody extends ActionBody {
    public static final String DEFAULT_TRANSFER_ENCODING = "binary";

    private static final class MimePart {
        private final ActionBody body;
        private final String name;
        private final String transferEncoding;
        private final boolean isFirst;
        private final String boundary;

        private byte[] partBoundary;
        private byte[] partHeader;
        private boolean isBuilt;

        public MimePart(String name, String transferEncoding, ActionBody body, String boundary,
                        boolean isFirst) {
            this.name = name;
            this.transferEncoding = transferEncoding;
            this.body = body;
            this.isFirst = isFirst;
            this.boundary = boundary;
        }

        public void writeTo(OutputStream out) throws IOException {
            build();
            out.write(partBoundary);
            out.write(partHeader);
            body.writeTo(out);
        }

        public long size() {
            build();
            if (body.length() > -1) {
                return body.length() + partBoundary.length + partHeader.length;
            } else {
                return -1;
            }
        }

        private void build() {
            if (isBuilt) return;
            partBoundary = buildBoundary(boundary, isFirst, false);
            partHeader = buildHeader(name, transferEncoding, body);
            isBuilt = true;
        }
    }

    private final List<MimePart> mimeParts = new LinkedList<MimePart>();

    private final byte[] footer;
    private final String boundary;
    private long length;

    public MultipartRequestBody() {
        this(UUID.randomUUID().toString());
    }

    MultipartRequestBody(String boundary) {
        super("multipart/form-data; boundary=" + boundary);
        this.boundary = boundary;
        footer = buildBoundary(boundary, false, true);
        length = footer.length;
    }

    @Override
    public byte[] getContent() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            for (MimePart part : mimeParts) {
                part.writeTo(out);
            }
            out.write(footer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    List<byte[]> getParts() throws IOException {
        List<byte[]> parts = new ArrayList<byte[]>(mimeParts.size());
        for (MimePart part : mimeParts) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            part.writeTo(bos);
            parts.add(bos.toByteArray());
        }
        return parts;
    }

    public void addPart(String name, ActionBody body) {
        addPart(name, DEFAULT_TRANSFER_ENCODING, body);
    }

    public void addPart(String name, String transferEncoding, ActionBody body) {
        if (name == null) {
            throw new NullPointerException("Part name must not be null.");
        }
        if (transferEncoding == null) {
            throw new NullPointerException("Transfer encoding must not be null.");
        }
        if (body == null) {
            throw new NullPointerException("Part body must not be null.");
        }

        MimePart part = new MimePart(name, transferEncoding, body, boundary, mimeParts.isEmpty());
        mimeParts.add(part);

        long size = part.size();
        if (size == -1) {
            length = -1;
        } else if (length != -1) {
            length += size;
        }
    }

    public int getPartCount() {
        return mimeParts.size();
    }

    @Override
    public long length() {
        return length;
    }

    private static byte[] buildBoundary(String boundary, boolean first, boolean last) {
        try {
            StringBuilder sb = new StringBuilder(boundary.length() + 8);

            if (!first) {
                sb.append("\r\n");
            }
            sb.append("--");
            sb.append(boundary);
            if (last) {
                sb.append("--");
            }
            sb.append("\r\n");
            return sb.toString().getBytes("UTF-8");
        } catch (IOException ex) {
            throw new RuntimeException("Unable to write multipart boundary", ex);
        }
    }

    private static byte[] buildHeader(String name, String transferEncoding, ActionBody value) {
        try {
            StringBuilder headers = new StringBuilder(128);

            headers.append("Content-Disposition: form-data; name=\"");
            headers.append(name);

            String fileName = value.fileName();
            if (fileName != null) {
                headers.append("\"; filename=\"");
                headers.append(fileName);
            }

            headers.append("\"\r\nContent-Type: ");
            headers.append(value.mimeType());

            long length = value.length();
            if (length != -1) {
                headers.append("\r\nContent-Length: ").append(length);
            }

            headers.append("\r\nContent-Transfer-Encoding: ");
            headers.append(transferEncoding);
            headers.append("\r\n\r\n");

            return headers.toString().getBytes("UTF-8");
        } catch (IOException ex) {
            throw new RuntimeException("Unable to write multipart header", ex);
        }
    }
}
