package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.OpenOption;
import java.util.Collection;

import static java.nio.file.StandardOpenOption.*;

/**
 * For read-only access to a git object, we don't need (or want) to read the entire thing into
 * memory.
 */
public class GfsSeekableReadOnlyByteChannel implements SeekableByteChannel {

    private final FileNode file;
    private InputStream stream = null;
    private long position = 0;
    boolean isOpen = true;
    // see skip() for details
    private long manualSkip;

    GfsSeekableReadOnlyByteChannel(FileNode file, Collection<? extends OpenOption> options) throws IOException {
        this.file = file;
        if(options.contains(APPEND)) {
            position = file.getSize();
        } else {
            stream = file.getInputStream();
        }
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        if (!isOpen)
            throw new ClosedChannelException();
        if (stream == null)
            return -1;
        if (manualSkip > 0) {
            //see skip() for details
            byte[] junk = new byte[(int)Math.min(65536, manualSkip)];
            while (manualSkip > 0) {
                manualSkip -= stream.read(junk, 0, (int)Math.min(junk.length, manualSkip));
            }
        }
        int result = stream.read(dst.array(), dst.arrayOffset() + dst.position(), dst.remaining());
        position += result;
        return result;
    }

    @Override
    public int write(ByteBuffer byteBuffer) throws IOException {
        throw new NonWritableChannelException();
    }

    @Override
    public long position() throws IOException {
        return position;
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        if (newPosition < position) {
            if (stream != null)
                stream.close();
            stream = file.getInputStream();
            position = 0;
            manualSkip = 0;
        }
        if (newPosition >= size()) {
            position = newPosition;
            stream = null;
        }
        while (newPosition > position) {
            long skipped = stream.skip(newPosition - position);
            if (skipped == 0) {
                // Incomprehensibly, InputStream.skip() is allowed to return
                // zero for no reason at all.  We could keep looping in that
                // case, but for all we know, we'regoing to just keep getting
                // zeros. So instead, we'll set this variable and handle
                // it in read()
                manualSkip = newPosition - position;
                break;
            }
            position += skipped;
        }
        return this;
    }

    @Override
    public long size() throws IOException {
        return file.getSize();
    }

    @Override
    public SeekableByteChannel truncate(long l) throws IOException {
        throw new NonWritableChannelException();
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public void close() throws IOException {
        if (stream != null)
            stream.close();
        isOpen = false;
    }
}
