package com.beijunyi.parallelgit.filesystem.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;

public class FileNode extends Node {

  private byte[] bytes;
  private long size = -1;

  private FileNode(@Nonnull NodeType type, @Nullable AnyObjectId object, @Nonnull DirectoryNode parent) {
    super(type, object, parent);
  }

  private FileNode(@Nonnull NodeType type, @Nonnull DirectoryNode parent) {
    this(type, null, parent);
    dirty = true;
  }

  @Nonnull
  protected static FileNode forBlobObject(@Nonnull AnyObjectId object, @Nonnull NodeType type, @Nonnull DirectoryNode parent) {
    return new FileNode(type, object, parent);
  }

  @Nonnull
  public static FileNode newFile(boolean executable, @Nonnull DirectoryNode parent) {
    return new FileNode(executable ? NodeType.EXECUTABLE_FILE : NodeType.NON_EXECUTABLE_FILE, parent);
  }

  @Nullable
  public byte[] getBytes() {
    return bytes;
  }

  public void setBytes(@Nullable byte[] bytes) {
    this.bytes = bytes;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public void loadContent(@Nonnull byte[] bytes) {
    setBytes(bytes);
    setSize(bytes.length);
  }

  public void updateContent(@Nonnull byte[] bytes) {
    loadContent(bytes);
    markDirty();
  }

}
