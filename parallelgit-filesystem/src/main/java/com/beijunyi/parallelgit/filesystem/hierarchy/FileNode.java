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
import org.eclipse.jgit.lib.ObjectReader;

public class FileNode extends TreeNode {

  private byte[] bytes;
  private Collection<GitSeekableByteChannel> channels = new LinkedList<>();

  public FileNode(@Nonnull TreeNodeType type, @Nonnull ObjectReader reader) {
    super(type, reader);
  }

  @Nonnull
  public static FileNode forRegularFileObject(@Nonnull AnyObjectId object, @Nonnull ObjectReader reader) {
    FileNode node = new FileNode(TreeNodeType.NON_EXECUTABLE_FILE, reader);
    node.object = object;
    return node;
  }

  @Nonnull
  public static FileNode forExecutableFileObject(@Nonnull AnyObjectId object, @Nonnull ObjectReader reader) {
    FileNode node = new FileNode(TreeNodeType.EXECUTABLE_FILE, reader);
    node.object = object;
    return node;
  }

  @Nonnull
  public static FileNode forSymlinkBlob(@Nonnull AnyObjectId object, @Nonnull ObjectReader reader) {
    FileNode node = new FileNode(TreeNodeType.SYMBOLIC_LINK, reader);
    node.object = object;
    return node;
  }

  @Override
  protected void doLoad() throws IOException {
    bytes = reader.open(object).getBytes();
  }

  @Override
  public synchronized void lock() throws AccessDeniedException {
    if(!channels.isEmpty())
      failLock();
    super.lock();
  }

  @Nonnull
  public synchronized GitSeekableByteChannel newChannel(@Nonnull Set<? extends OpenOption> options) throws AccessDeniedException {
    if(locked)
      failLock();
    GitSeekableByteChannel channel = new GitSeekableByteChannel(bytes, options, this);
    channels.add(channel);
    return channel;
  }

  public synchronized void removeChannel(@Nonnull GitSeekableByteChannel channel) {
    channels.remove(channel);
  }
}
