package com.beijunyi.parallelgit.filesystem.hierarchy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.AccessDeniedException;
import java.util.Collection;
import java.util.LinkedList;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.io.GitSeekableByteChannel;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectReader;

public class FileNode extends TreeNode {

  private ByteBuffer buffer;
  private Collection<GitSeekableByteChannel> readChannels = new LinkedList<>();
  private Collection<GitSeekableByteChannel> writeChannels = new LinkedList<>();

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
    buffer = ByteBuffer.wrap(reader.open(object).getBytes());
  }

  @Override
  synchronized public void lock() throws AccessDeniedException {
    if(!readChannels.isEmpty() || !writeChannels.isEmpty())
      denyAccess();
    super.lock();
  }

}
