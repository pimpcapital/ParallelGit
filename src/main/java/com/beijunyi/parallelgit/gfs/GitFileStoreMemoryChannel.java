package com.beijunyi.parallelgit.gfs;

import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nonnull;

public class GitFileStoreMemoryChannel implements SeekableByteChannel {

  private final GitFileStore store;
  private final String pathStr;
  private int position;
  private byte[] buf;
  private boolean modified = false;

  private Collection<GitSeekableByteChannel> attachedChannels = new LinkedList<>();

  private final Lock lock = new ReentrantLock();

  GitFileStoreMemoryChannel(@Nonnull GitFileStore store, @Nonnull String pathStr, @Nonnull byte[] buf) {
    this.store = store;
    this.pathStr = pathStr;
    this.buf = buf;
    position = buf.length;
  }

  GitFileStoreMemoryChannel(@Nonnull GitFileStore store, @Nonnull String pathStr) {
    this(store, pathStr, new byte[0]);
  }

  /**
   * Reads a sequence of bytes from this channel into the given buffer.
   *
   * @param   destination
   *          a byte buffer to which this channel can copy data.
   * @return  total number of bytes copied to the destination.
   */
  @Override
  public int read(@Nonnull ByteBuffer destination) {
    int capacity = destination.remaining();
    int available, size;

    available = buf.length - position;
    if(available <= 0)
      return -1;

    size = Math.min(available, capacity);
    destination.put(buf, position, size);

    position += size;
    return size;
  }

  /**
   * Writes a sequence of bytes to this channel from the given buffer.
   *
   * @param   source
   *          a byte buffer from which this channel can copy data
   * @return  total number of bytes copied from the source.
   */
  @Override
   public int write(@Nonnull ByteBuffer source) {
     int size = source.remaining();

     if(size != 0)
       modified = true;
     else
       return 0;

     byte[] data = new byte[size];
     source.get(data);

     int startPos = Math.min(position, buf.length);
     int endPos = startPos + size;
     int newSize = Math.max(buf.length, endPos);
     if(newSize > buf.length) {
       byte[] newBuf = new byte[newSize];
       System.arraycopy(buf, 0, newBuf, 0, startPos);
       buf = newBuf;
     }
     System.arraycopy(data, 0, buf, startPos, size);
     position = endPos;

     return size;
   }

  /**
   * Returns this channel's position.
   *
   * @return  This channel's position, a non-negative integer counting the number of bytes from the beginning of the
   * {@link #buf byte array buffer} to the current position.
   */
  @Override
  public long position() {
    return position;
  }

  /**
   * Sets this channel's position.
   *
   * Setting the position to a value that is greater than the current size is legal but does not change the size of the
   * buffer. A later attempt to read bytes at such a position will immediately return an end-of-file indication. A later
   * attempt to write bytes at such a position will cause the buffer to grow to accommodate the new bytes; the values of
   * any bytes between the previous end-of-file and the newly-written bytes are unspecified.
   *
   * @param   newPosition
   *          the new position, a byte count between 0 and {@link Integer#MAX_VALUE 2147483647} inclusive
   * @return  this channel
   */
  @Nonnull
  @Override
  public SeekableByteChannel position(long newPosition) {
    if(newPosition > Integer.MAX_VALUE || newPosition < 0)
      throw new IllegalArgumentException("Valid position for this channel is between 0 and " + Integer.MAX_VALUE);

    this.position = (int) newPosition;

    return this;
  }

  /**
   * Returns the length of the {@link #buf byte array buffer}.
   *
   * @return  the length of the {@link #buf byte array buffer}
   */
  @Override
  public long size() {
    return buf.length;
  }

  /**
   * Truncates the {@link #buf byte array buffer} to the given size.
   *
   * If the given size is less than the current size then the buffer is truncated, discarding any bytes beyond the new
   * end. If the given size is greater than or equal to the current size then the buffer is not modified. In either
   * case, if the current position is greater than the given size then it is set to that size.
   *
   * @param   size
   *          a byte count between 0 and {@link Integer#MAX_VALUE 2147483647} inclusive
   * @return  this channel
   *
   * @throws  IllegalArgumentException
   *          if the new size is negative or greater than {@link Integer#MAX_VALUE 2147483647}
   */
  @Nonnull
  @Override
  public GitFileStoreMemoryChannel truncate(long size) throws IllegalArgumentException {
    if(size < 0 || size > Integer.MAX_VALUE)
      throw new IllegalArgumentException("This implementation permits a size of 0 to " + Integer.MAX_VALUE + " inclusive");

    int newSize = (int) size;

    if(position > newSize)
      position = newSize;

    if(buf.length > newSize) {
      modified = true;
      byte[] newBuf = new byte[newSize];
      System.arraycopy(buf, 0, newBuf, 0, newSize);
      buf = newBuf;
    }

    return this;
  }

  /**
   * @return  {@code true}.
   */
  @Override
  public boolean isOpen() {
    return true;
  }

  /**
   * Closes all the {@code GitSeekableByteChannel}s attached to this {@code GitFileStoreMemoryChannel}.
   */
  @Override
  public void close() {
    for(GitSeekableByteChannel channel : attachedChannels)
      channel.close();
    attachedChannels.clear();
  }

  /**
   * Returns the value of the {@link #modified modified} flag.
   *
   * @return  the value of the {@link #modified modified} flag
   */
  boolean isModified() {
    return modified;
  }

  /**
   * Sets the {@link #modified modified} flag.
   *
   * @param   modified
   *          the value to set to
   */
  void setModified(boolean modified) {
    this.modified = modified;
  }

  /**
   * Attaches a {@code GitSeekableByteChannel} to this {@code GitFileStoreMemoryChannel}.
   *
   * @param   channel
   *          the {@code GitSeekableByteChannel} to attach
   */
  void attach(@Nonnull GitSeekableByteChannel channel) {
    attachedChannels.add(channel);
  }

  /**
   * Detaches a {@code GitSeekableByteChannel} from this {@code GitFileStoreMemoryChannel}.
   *
   * If this {@code GitFileStoreMemoryChannel} is not {@link #modified modified} and the {@code GitSeekableByteChannel}
   * to detach is the only instance attached to this channel, this method will hint the parent {@code GitFileStore} to
   * {@link GitFileStore#garbageCollectChannel garbage collect} this {@code GitFileStoreMemoryChannel} after the given
   * instance is successfully detached.
   *
   * @param   channel
   *          the {@code GitSeekableByteChannel} to detach
   */
  void detach(@Nonnull GitSeekableByteChannel channel) {
    if(!attachedChannels.remove(channel))
      throw new IllegalArgumentException();
    // if the buffer hasn't been modified and no child channel attaches to this
    if(!modified && attachedChannels.isEmpty()) {
      // garbage collect this channel
      store.garbageCollectChannel(this);
    }
  }

  /**
   * Acquires the {@link #buf buffer} lock.
   *
   * If the lock is not available then the current thread becomes disabled for thread scheduling purposes and lies
   * dormant until the lock has been acquired.
   */
  void lockBuffer() {
    lock.lock();
  }

  /**
   * Releases the {@link #buf buffer} lock.
   */
  void releaseBuffer() {
    lock.unlock();
  }

  /**
   * Returns the number of {@code GitSeekableByteChannel} attached to this {@code GitFileStoreMemoryChannel}.
   *
   * @return  the number of {@code GitSeekableByteChannel} attached to this {@code GitFileStoreMemoryChannel}
   */
  int countAttachedChannels() {
    return attachedChannels.size();
  }

  /**
   * Creates a newly allocated byte array. Its size is the current size of this {@code GitFileStoreMemoryChannel} and
   * the valid contents of the buffer have been copied into it.
   *
   * @return  the current contents of this {@code GitFileStoreMemoryChannel} as a byte array.
   */
  @Nonnull
  public byte[] getBytes() {
    return buf;
  }

  /**
   * Returns the path to the file that this {@code GitFileStoreMemoryChannel} associates with.
   *
   * @return  the string path to the file that this {@code GitFileStoreMemoryChannel} associates with
   */
  @Nonnull
  public String getPathStr() {
    return pathStr;
  }
}
