package com.beijunyi.parallelgit.filesystem.hierarchy;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.OpenOption;
import java.util.*;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileStore;
import com.beijunyi.parallelgit.filesystem.io.GitSeekableByteChannel;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;

import static java.nio.file.StandardOpenOption.*;

public class FileNode extends Node {

  private byte[] bytes;
  private final Collection<GitSeekableByteChannel> channels = new LinkedList<>();

  protected FileNode(@Nonnull NodeType type, @Nonnull AnyObjectId object) {
    super(type, object);
  }

  protected FileNode(@Nonnull NodeType type) {
    this(type, ObjectId.zeroId());
    bytes = new byte[0];
    loaded = true;
    dirty = true;
  }

  @Nonnull
  protected static FileNode forBlobObject(@Nonnull AnyObjectId object, @Nonnull NodeType type) {
    return new FileNode(type, object);
  }

  @Nonnull
  protected static FileNode newFile(boolean executable) {
    return new FileNode(executable ? NodeType.EXECUTABLE_FILE : NodeType.NON_EXECUTABLE_FILE);
  }

  @Override
  protected void doLoad(@Nonnull GitFileStore store, boolean recursive) throws IOException {
    if(!loaded) {
      bytes = store.getBlobBytes(object);
      loaded = true;
    }
    if(recursive)
      dirty = true;
  }

  @Nonnull
  @Override
  public AnyObjectId doSave() throws IOException {
    if(!dirty)
      return object;
    return store().insertBlob(bytes);
  }

  @Override
  public synchronized void lock() throws AccessDeniedException {
    if(!channels.isEmpty())
      denyAccess();
    super.lock();
  }

  @Override
  protected long calculateSize() throws IOException {
    if(bytes != null)
      return bytes.length;
    return store().getBlobSize(object);
  }

  @Nonnull
  private Set<OpenOption> amendOpenOptions(@Nonnull Set<? extends OpenOption> options) {
    Set<OpenOption> ret = new HashSet<>(options);
    if(!options.contains(READ) && !options.contains(WRITE)) {
      if(options.contains(APPEND))
        ret.add(WRITE);
      else
        ret.add(READ);
    }
    return ret;
  }

  @Nonnull
  public synchronized GitSeekableByteChannel newChannel(@Nonnull Set<OpenOption> options) throws IOException {
    checkNotLocked();
    load();
    GitSeekableByteChannel channel = new GitSeekableByteChannel(bytes, amendOpenOptions(options), this);
    channels.add(channel);
    return channel;
  }

  public synchronized void removeChannel(@Nonnull GitSeekableByteChannel channel) {
    if(!channels.remove(channel))
      throw new IllegalArgumentException();
    bytes = channel.bytes();
  }

  @Nonnull
  @Override
  protected synchronized FileNode clone(boolean deepClone) throws IOException {
    FileNode clone = new FileNode(type, object);
    if(deepClone || dirty) {
      if(loaded) {
        clone.bytes = bytes.clone();
        clone.loaded = true;
        clone.dirty = true;
      } else
        clone.doLoad(store(), true);
    }
    return clone;
  }
}
