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

  public FileNode(@Nonnull TreeNodeType type) {
    super(type);
  }

  @Nonnull
  public static FileNode forRegularFileObject(@Nonnull AnyObjectId object) {
    FileNode node = new FileNode(TreeNodeType.NON_EXECUTABLE_FILE);
    node.object = object;
    return node;
  }

  @Nonnull
  public static FileNode forExecutableFileObject(@Nonnull AnyObjectId object) {
    FileNode node = new FileNode(TreeNodeType.EXECUTABLE_FILE);
    node.object = object;
    return node;
  }

  @Nonnull
  public static FileNode forSymlinkBlob(@Nonnull AnyObjectId object) {
    FileNode node = new FileNode(TreeNodeType.SYMBOLIC_LINK);
    node.object = object;
    return node;
  }

  @Override
  protected void doLoad(@Nonnull ObjectReader reader) throws IOException {
    bytes = reader.open(object).getBytes();
  }

  @Override
  synchronized public void lock() throws AccessDeniedException {
    if(!channels.isEmpty())
      denyAccess();
    super.lock();
  }

  @Nonnull
  synchronized public GitSeekableByteChannel newChannel(@Nonnull Set<? extends OpenOption> options) throws AccessDeniedException {
    if(locked)
      denyAccess();
    GitSeekableByteChannel channel = new GitSeekableByteChannel(bytes, options, this);
    channels.add(channel);
    return channel;
  }

  synchronized public void removeChannel(@Nonnull GitSeekableByteChannel channel) {
    channels.remove(channel);
  }
}
