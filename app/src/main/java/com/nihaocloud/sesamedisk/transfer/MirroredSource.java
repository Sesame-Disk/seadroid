package com.nihaocloud.sesamedisk.transfer;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import okio.Buffer;
import okio.Source;
import okio.Timeout;

public final class MirroredSource {

    private final Buffer buffer = new Buffer();
    private final Source source;
    private final AtomicBoolean sourceExhausted = new AtomicBoolean();
    private boolean closed = false;

    /**
     * Takes a bytes source and streams it to parallel sources, I.E Streaming bytes to two server in parallel.
     *
     * @param source The Bytes source you want to stream in parallel.
     */
    public MirroredSource(final Source source) {
        this.source = source;
    }

    /**
     * As soon as you read from the returned source, its output is copied and buffered. This buffer can then be read from
     * mirror().
     *
     * @return a byte source.
     */
    public final Source original() {
        return new okio.Source() {

            @Override public long read(final Buffer sink, final long byteCount) throws IOException {
                final long bytesRead = source.read(sink, byteCount);
                if (bytesRead > 0) {
                    synchronized (buffer) {
                        sink.copyTo(buffer, sink.size() - bytesRead, bytesRead);
                        // Notfiy the mirror to continue
                        buffer.notifyAll();
                    }
                } else {
                    synchronized (buffer) {
                        buffer.notifyAll();
                    }
                    sourceExhausted.set(true);
                }
                return bytesRead;
            }

            @Override public Timeout timeout() {
                return source.timeout();
            }

            @Override public void close() throws IOException {
                source.close();
                sourceExhausted.set(true);
                synchronized (buffer) {
                    buffer.notifyAll();
                }
            }
        };
    }

    /**
     * A byte source. Will emit all bytes emitted from original(). Will end when original() is exhausted.
     *
     * @return A bytes source.
     */
    public final Source mirror() {
        return new okio.Source() {

            @Override public long read(final Buffer sink, final long byteCount) throws IOException {
                if (closed) new IllegalStateException("reading closed source");
                while (!sourceExhausted.get()) {
                    // only need to synchronise on reads when the source is not exhausted.
                    synchronized (buffer) {
                        if (buffer.request(byteCount)) {
                            return buffer.read(sink, byteCount);
                        } else {
                            try {
                                buffer.wait(200);
                            } catch (final InterruptedException e) {
                                return -1;
                            }
                        }
                    }
                }
                return buffer.read(sink, byteCount);
            }

            @Override public Timeout timeout() {
                return new Timeout();
            }

            @Override public void close() throws IOException {
                buffer.clear();
                closed = true;
            }
        };
    }
}
