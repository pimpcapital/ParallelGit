package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.OpenOption;
import java.util.Collection;
import javax.annotation.Nonnull;

import static java.lang.System.arraycopy;
import static java.nio.file.StandardOpenOption.*;

public class GfsSeekableByteChannel implements SeekableByteChannel {

  private final FileNode file;
  private final boolean readable;
  private final boolean writable;
  private ByteBuffer buffer;
  private volatile boolean closed = false;

  GfsSeekableByteChannel(FileNode file, Collection<? extends OpenOption> options) throws IOException {
    this.file = file;
    buffer = ByteBuffer.wrap(options.contains(TRUNCATE_EXISTING) ? new byte[0] : file.getData());
    readable = options.contains(READ);
    writable = options.contains(WRITE);
    if(options.contains(APPEND)) buffer.position(buffer.limit());
  }

  @Override
  public int read(ByteBuffer dst) throws ClosedChannelException {
    checkClosed();
    checkReadAccess();
    synchronized(this) {
      if (!buffer.hasRemaining()) {
        return -1;
      }
      return copyBytes(dst, buffer);
    }
  }

  @Override
  public int write(ByteBuffer src) throws ClosedChannelException {
    checkClosed();
    checkWriteAccess();
    synchronized(this) {
      if(buffer.remaining() < src.remaining()) {
        int position = buffer.position();
        byte[] bytes = new byte[position + src.remaining()];
        arraycopy(buffer.array(), buffer.arrayOffset(), bytes, 0, position);
        buffer = ByteBuffer.wrap(bytes);
        buffer.position(position);
      }
      return copyBytes(buffer, src);
    }
  }

  @Override
  public long position() throws ClosedChannelException {
    checkClosed();
    return buffer.position();
  }

  private static int toInt(long num) {
    if(num < 0 || num >= Integer.MAX_VALUE)
      throw new IllegalArgumentException("Input must be between 0 and " + Integer.MAX_VALUE);
    return (int) num;
  }

  @Override
  public GfsSeekableByteChannel position(long newPosition) throws ClosedChannelException {
    checkClosed();
    synchronized(this) {
      buffer.position(toInt(newPosition));
    }
    return this;
  }

  @Override
  public long size() throws ClosedChannelException {
    checkClosed();
    return buffer.limit();
  }

  @Override
  public GfsSeekableByteChannel truncate(long size) throws NonWritableChannelException, ClosedChannelException, IllegalArgumentException {
    checkClosed();
    checkWriteAccess();
    synchronized(this) {
      buffer.limit(toInt(size));
    }
    return this;
  }

  @Override
  public boolean isOpen() {
    return !closed;
  }

  @Nonnull
  public byte[] getBytes() {
    synchronized(this) {
      byte[] bytes = new byte[buffer.limit()];
      arraycopy(buffer.array(), 0, bytes, 0, bytes.length);
      return bytes;
    }

  }

  @Override
  public void close() {
    if(closed)
      return;
    synchronized(this) {
      if(!closed) {
        closed = true;
        file.setBytes(getBytes());
      }
    }
  }

  private void checkClosed() throws ClosedChannelException {
    if(!isOpen()) throw new ClosedChannelException();
  }

  private void checkReadAccess() throws NonReadableChannelException {
    if(!readable) throw new NonReadableChannelException();
  }

  private void checkWriteAccess() throws NonWritableChannelException {
    if(!writable) throw new NonWritableChannelException();
  }

  private static int copyBytes(ByteBuffer dst, ByteBuffer src) {
    int remaining = Math.min(src.remaining(), dst.remaining());
    ByteBuffer toCopy = src.slice();
    toCopy.limit(remaining);
    dst.put(toCopy);
    src.position(src.position() + remaining);
    return remaining;
  }

}
