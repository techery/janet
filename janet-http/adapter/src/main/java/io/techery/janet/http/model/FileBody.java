package io.techery.janet.http.model;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import io.techery.janet.body.ActionBody;

public class FileBody extends ActionBody {
    private static final int BUFFER_SIZE = 4096;

    private final String mimeType;
    private final File file;

    public FileBody(String mimeType, File file) {
        super(mimeType);
        if (mimeType == null) {
            throw new NullPointerException("mimeType");
        }
        if (file == null) {
            throw new NullPointerException("file");
        }
        this.mimeType = mimeType;
        this.file = file;
    }

    public File file() {
        return file;
    }

    @Override
    public String mimeType() {
        return mimeType;
    }

    @Override
    public long length() {
        return file.length();
    }

    public String fileName() {
        return file.getName();
    }

    @Override
    public byte[] getContent() throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        FileInputStream in = new FileInputStream(file);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } finally {
            in.close();
        }
        return out.toByteArray();
    }

    public void moveTo(FileBody destination) throws IOException {
        if (!mimeType().equals(destination.mimeType())) {
            throw new IOException("Type mismatch.");
        }
        if (!file.renameTo(destination.file())) {
            throw new IOException("Rename failed!");
        }
    }

    @Override
    public String toString() {
        return file.getAbsolutePath() + " (" + mimeType() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof FileBody) {
            FileBody rhs = (FileBody) o;
            return file.equals(rhs.file);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }
}
