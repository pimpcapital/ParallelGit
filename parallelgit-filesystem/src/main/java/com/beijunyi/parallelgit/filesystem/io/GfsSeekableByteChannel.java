package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GfsDataService;

public class GfsSeekableByteChannel implements SeekableByteChannel {

  private final FileNode file;
  private final boolean readable;
  private final boolean writable;
  private ByteBuffer buffer;
  private volatile boolean closed = false;

  GfsSeekableByteChannel(@Nonnull FileNode file, @Nonnull GfsDataService gds, @Nonnull Set<OpenOption> options) throws IOException {
    this.file = file;
    buffer = ByteBuffer.wrap(options.contains(StandardOpenOption.TRUNCATE_EXISTING) ? new byte[0] : GfsIO.getFileData(file, gds).clone());
    readable = options.contains(StandardOpenOption.READ);
    writable = options.contains(StandardOpenOption.WRITE);
    if(options.contains(StandardOpenOption.APPEND))
      buffer.position(buffer.limit());
  }

  private static int copyBytes(@Nonnull ByteBuffer dst, @Nonnull ByteBuffer src) {
    int remaining = Math.min(src.remaining(), dst.remaining());
    for(int i = 0; i < remaining; i++)
      dst.put(src.get());
    return remaining;
  }

  @Override
  public int read(@Nonnull ByteBuffer dst) throws ClosedChannelException {
    checkClosed();
    checkReadAccess();
    synchronized(this) {
      return copyBytes(dst, buffer);
    }
  }

  @Override
  public int write(@Nonnull ByteBuffer src) throws ClosedChannelException {
    checkClosed();
    checkWriteAccess();
    synchronized(this) {
      if(buffer.remaining() < src.remaining()) {
        int position = buffer.position();
        byte[] bytes = new byte[position + src.remaining()];
        System.arraycopy(buffer.array(), 0, bytes, 0, position);
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

  private static int long2Int(long num) {
    if(num < 0 || num >= Integer.MAX_VALUE)
      throw new IllegalArgumentException("Input must be between 0 and " + Integer.MAX_VALUE);
    return (int) num;
  }

  @Override
  public GfsSeekableByteChannel position(long newPosition) throws ClosedChannelException {
    checkClosed();
    synchronized(this) {
      buffer.position(long2Int(newPosition));
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
      buffer.limit(long2Int(size));
    }
    return this;
  }

  @Override
  public boolean isOpen() {
    return !closed;
  }

  public boolean isReadable() {
    return readable;
  }

  public boolean isWritable() {
    return writable;
  }

  @Nonnull
  public byte[] getBytes() {
    synchronized(this) {
      byte[] bytes = new byte[buffer.limit()];
      System.arraycopy(buffer.array(), 0, bytes, 0, bytes.length);
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
    if(!isOpen())
      throw new ClosedChannelException();
  }

  private void checkReadAccess() throws NonReadableChannelException {
    if(!readable)
      throw new NonReadableChannelException();
  }

  private void checkWriteAccess() throws NonWritableChannelException {
    if(!writable)
      throw new NonWritableChannelException();
  }

}
