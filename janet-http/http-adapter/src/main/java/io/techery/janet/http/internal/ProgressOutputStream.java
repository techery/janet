package io.techery.janet.http.internal;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import io.techery.janet.http.HttpClient;

public final class ProgressOutputStream extends FilterOutputStream {

    private final ProgressListener listener;
    private final long threshold;
    private long progress;
    private long lastProgress;

    public ProgressOutputStream(OutputStream stream, ProgressListener listener, long threshold) {
        super(stream);
        this.listener = listener;
        this.threshold = threshold;
    }

    public ProgressOutputStream(OutputStream stream, ProgressListener listener) {
        this(stream, listener, HttpClient.PROGRESS_THRESHOLD);
    }

    @Override
    public void write(int b) throws IOException {
        super.write(b);
        progress++;
        if (progress > lastProgress + threshold) {
            listener.onProgressChanged(progress);
            lastProgress = progress;
        }
    }

    public long getThreshold() {
        return threshold;
    }

    public interface ProgressListener {
        void onProgressChanged(long bytesWritten);
    }
}