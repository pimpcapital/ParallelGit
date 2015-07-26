package com.beijunyi.parallelgit.filesystem.hierarchy;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.OpenOption;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.io.GitSeekableByteChannel;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;

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
  public static FileNode forRegularFileObject(@Nonnull AnyObjectId object) {
    return new FileNode(NodeType.NON_EXECUTABLE_FILE, object);
  }

  @Nonnull
  public static FileNode forExecutableFileObject(@Nonnull AnyObjectId object) {
    return new FileNode(NodeType.EXECUTABLE_FILE, object);
  }

  @Nonnull
  public static FileNode forSymlinkBlob(@Nonnull AnyObjectId object) {
    return new FileNode(NodeType.SYMBOLIC_LINK, object);
  }

  @Nonnull
  public static FileNode newFile() {
    return new FileNode(NodeType.NON_EXECUTABLE_FILE);
  }

  @Override
  protected void doLoad() throws IOException {
    bytes = reader.open(object).getBytes();
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
    return reader.getObjectSize(object, Constants.OBJ_BLOB);
  }

  @Nonnull
  public synchronized GitSeekableByteChannel newChannel(@Nonnull Set<? extends OpenOption> options) throws AccessDeniedException {
    if(locked)
      denyAccess();
    GitSeekableByteChannel channel = new GitSeekableByteChannel(bytes, options, this);
    channels.add(channel);
    return channel;
  }

  public synchronized void removeChannel(@Nonnull GitSeekableByteChannel channel) {
    if(!channels.remove(channel))
      throw new IllegalArgumentException();
  }

  @Nonnull
  @Override
  protected synchronized FileNode prepareClone() {
    FileNode clone = new FileNode(type, object);
    clone.bytes = bytes.clone();
    return clone;
  }
}
