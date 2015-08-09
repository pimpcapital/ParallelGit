package com.beijunyi.parallelgit.filesystem.io;

import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.hierarchy.FileNode;

public class GitSeekableByteChannel implements SeekableByteChannel {

  private final FileNode parent;
  private final boolean readable;
  private final boolean writable;
  private ByteBuffer buffer;
  private volatile boolean closed = false;

  public GitSeekableByteChannel(@Nonnull byte[] bytes, @Nonnull Set<? extends OpenOption> options, @Nonnull FileNode node) {
    this.parent = node;
    readable = options.contains(StandardOpenOption.READ);
    writable = options.contains(StandardOpenOption.WRITE);
    buffer = ByteBuffer.wrap(bytes);
    if(writable && options.contains(StandardOpenOption.TRUNCATE_EXISTING))
      buffer.limit(0);
    if(writable & options.contains(StandardOpenOption.APPEND))
      buffer.position(buffer.limit());
  }

  private static int copyBytes(@Nonnull ByteBuffer dst, @Nonnull ByteBuffer src) {
    int remaining = Math.min(src.remaining(), dst.remaining());
    for(int i = 0; i < remaining; i++)
      dst.put(src.get());
    return remaining;
  }

  /**
   * Reads a sequence of bytes from this channel into the given buffer.
   *
   * Bytes are read starting at this channel's current position, and then the position is updated with the number of
   * bytes actually read.
   *
   * @return  the number of bytes read from this channel
   * @throws  ClosedChannelException
   *          if this channel is closed
   */
  @Override
  public int read(@Nonnull ByteBuffer dst) throws ClosedChannelException {
    checkClosed();
    checkReadAccess();
    synchronized(this) {
      return copyBytes(dst, buffer);
    }
  }

  /**
   * Writes a sequence of bytes to this channel from the given buffer.
   *
   * Bytes are written starting at this channel's current position, unless the channel is opened with the {@link
   * StandardOpenOption#APPEND} option, in which case the position is first advanced to the end. The buffer of the
   * parent {@code GitFileStoreMemoryChannel} is grown, if necessary, to accommodate the written bytes, and then the
   * position is updated with the number of bytes actually written.
   *
   * @return  the number of bytes written to this channel
   * @throws  ClosedChannelException
   *          if this channel is closed
   */
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

  /**
   * Returns this channel's position.
   *
   * @return  this channel's position
   * @throws  ClosedChannelException
   *          if this channel is closed
   */
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

  /**
   * Sets this channel's position.
   *
   * Setting the position to a value that is greater than the current size is legal but does not change the size of the
   * entity. A later attempt to read bytes at such a position will immediately return an end-of-file indication. A later
   * attempt to write bytes at such a position will cause the entity to grow to accommodate the new bytes; the values of
   * any bytes between the previous end-of-file and the newly-written bytes are unspecified.
   *
   * Setting the channel's position is not recommended when it is opened with the {@link StandardOpenOption#APPEND}
   * option. When opened for append, the position is first advanced to the end before writing.
   *
   * @param   newPosition
   *          the new position
   * @return  this channel
   * @throws  ClosedChannelException
   *          if this channel is closed
   */
  @Override
  public GitSeekableByteChannel position(long newPosition) throws ClosedChannelException {
    checkClosed();
    synchronized(this) {
      buffer.position(long2Int(newPosition));
    }
    return this;
  }

  /**
   * Returns the current size of the buffer of the parent {@code GitFileStoreMemoryChannel}.
   *
   * @return  the current size, measured in bytes
   * @throws  ClosedChannelException
   *          if this channel is closed
   */
  @Override
  public long size() throws ClosedChannelException {
    checkClosed();
    return buffer.limit();
  }

  /**
   * Truncates the buffer of the parent {@code GitFileStoreMemoryChannel}.
   *
   * If the given size is less than the current size then the buffer is truncated, discarding any bytes beyond the new
   * end. If the given size is greater than or equal to the current size then the buffer is not modified. In either
   * case, if the current position is greater than the given size then it is set to that size.
   *
   * @param   size
   *          the new size
   * @return  this channel
   * @throws  NonWritableChannelException
   *          if this channel was not opened for writing
   * @throws  ClosedChannelException
   *          If this channel is closed
   * @throws  IllegalArgumentException
   *          If the new size is negative or greater than {@link Integer#MAX_VALUE}
   */
  @Override
  public GitSeekableByteChannel truncate(long size) throws NonWritableChannelException, ClosedChannelException, IllegalArgumentException {
    checkClosed();
    checkWriteAccess();
    synchronized(this) {
      buffer.limit(long2Int(size));
    }
    return this;
  }

  /**
   * Tells if this channel is open.
   *
   * @return  {@code true} if this channel is open
   */
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

  /**
   * Closes this channel.
   *
   * After a channel is closed, any further attempt to invoke I/O operations upon it will cause a {@link
   * ClosedChannelException} to be thrown.
   *
   * If this channel is already closed then invoking this method has no effect.
   *
   * This method may be invoked at any time. If some other thread has already invoked it, however, then another
   * invocation will block until the first invocation is complete, after which it will return without effect.
   */
  @Override
  public void close() {
    if(closed)
      return;
    synchronized(this) {
      if(!closed) {
        closed = true;
        parent.updateContent(getBytes());
      }
    }
  }

  /**
   * Checks if this channel is closed.
   *
   * @throws  ClosedChannelException
   *          if this channel is closed
   */
  private void checkClosed() throws ClosedChannelException {
    if(!isOpen())
      throw new ClosedChannelException();
  }

  /**
   * Checks if this channel was opened for reading.
   *
   * @throws  NonReadableChannelException
   *          if this channel was not opened for reading
   */
  private void checkReadAccess() throws NonReadableChannelException {
    if(!readable)
      throw new NonReadableChannelException();
  }

  /**
   * Checks if this channel was opened for writing
   *
   * @throws  NonWritableChannelException
   *          if this channel was not opened for writing
   */
  private void checkWriteAccess() throws NonWritableChannelException {
    if(!writable)
      throw new NonWritableChannelException();
  }

}
