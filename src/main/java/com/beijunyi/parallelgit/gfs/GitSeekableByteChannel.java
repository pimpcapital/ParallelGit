package com.beijunyi.parallelgit.gfs;

import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import javax.annotation.Nonnull;

public class GitSeekableByteChannel implements SeekableByteChannel {

  private final GitFileStoreMemoryChannel memoryChannel;
  private final Set<? extends OpenOption> options;

  private long position = 0;
  private boolean closed = false;

  public GitSeekableByteChannel(@Nonnull GitFileStoreMemoryChannel memoryChannel, @Nonnull Set<? extends OpenOption> options) {
    this.memoryChannel = memoryChannel;
    this.options = options;
    memoryChannel.attach(this);
    if(options.contains(StandardOpenOption.WRITE)) {
      boolean truncate = options.contains(StandardOpenOption.TRUNCATE_EXISTING);
      boolean append = !truncate && options.contains(StandardOpenOption.APPEND);
      if(truncate || append) {
        memoryChannel.lockBuffer();
        try {
          if(truncate)
            memoryChannel.truncate(0);
          else
            position = memoryChannel.size();
        } finally {
          memoryChannel.releaseBuffer();
        }
      }
    }
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
    checkReadAccess();
    synchronized(this) {
      checkClosed();
      memoryChannel.lockBuffer();
      try {
        memoryChannel.position(position);
        int result = memoryChannel.read(dst);
        position = memoryChannel.position();
        return result;
      } finally {
        memoryChannel.releaseBuffer();
      }
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
    checkWriteAccess();
    synchronized(this) {
      checkClosed();
      memoryChannel.lockBuffer();
      try {
        memoryChannel.position(position);
        int result = memoryChannel.write(src);
        position = memoryChannel.position();
        return result;
      } finally {
        memoryChannel.releaseBuffer();
      }
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
    synchronized(this) {
      checkClosed();
      return position;
    }
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
    synchronized(this) {
      checkClosed();
      position = newPosition;
      return this;
    }
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
    synchronized(this) {
      checkClosed();
      memoryChannel.lockBuffer();
      try {
        return memoryChannel.size();
      } finally {
        memoryChannel.releaseBuffer();
      }
    }
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
    checkWriteAccess();
    synchronized(this) {
      checkClosed();
      memoryChannel.lockBuffer();
      try {
        memoryChannel.position(position);
        memoryChannel.truncate(size);
        position = memoryChannel.position();
        return this;
      } finally {
        memoryChannel.releaseBuffer();
      }
    }
  }

  /**
   * Tells if this channel is open.
   *
   * @return  {@code true} if this channel is open
   */
  @Override
  public boolean isOpen() {
    synchronized(this) {
      return !closed;
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
    synchronized(this) {
      if(isOpen()) {
        closed = true;
        memoryChannel.detach(this);
      }
    }
  }

  /**
   * Tells if this channel was opened for reading
   *
   * @return  {@code true} if this channel was opened for reading
   */
  boolean isReadable() {
    return options.contains(StandardOpenOption.READ);
  }

  /**
   * Tells if this channel was opened for writing
   *
   * @return  {@code true} if this channel was opened for writing
   */
  boolean isWritable() {
    return options.contains(StandardOpenOption.WRITE);
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
    if(!isReadable())
      throw new NonReadableChannelException();
  }

  /**
   * Checks if this channel was opened for writing
   *
   * @throws  NonWritableChannelException
   *          if this channel was not opened for writing
   */
  private void checkWriteAccess() throws NonWritableChannelException {
    if(!isWritable())
      throw new NonWritableChannelException();
  }
}
