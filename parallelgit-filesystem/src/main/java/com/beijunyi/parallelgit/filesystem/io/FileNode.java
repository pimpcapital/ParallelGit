package com.beijunyi.parallelgit.filesystem.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

public class FileNode extends Node {

  private byte[] bytes;
  private long size = -1;

  private FileNode(@Nonnull FileMode mode, @Nullable AnyObjectId object, @Nonnull DirectoryNode parent) {
    super(mode, object, parent);
  }

  private FileNode(@Nonnull FileMode mode, @Nonnull DirectoryNode parent) {
    this(mode, null, parent);
    dirty = true;
  }

  @Nonnull
  protected static FileNode forBlobObject(@Nonnull AnyObjectId object, @Nonnull FileMode mode, @Nonnull DirectoryNode parent) {
    return new FileNode(mode, object, parent);
  }

  @Nonnull
  public static FileNode forBytes(@Nonnull byte[] bytes, @Nonnull FileMode mode, @Nonnull DirectoryNode parent) {
    FileNode ret = new FileNode(mode, parent);
    ret.setBytes(bytes);
    return ret;
  }

  @Nonnull
  public static FileNode newFile(boolean executable, @Nonnull DirectoryNode parent) {
    return new FileNode(executable ? FileMode.EXECUTABLE_FILE : FileMode.REGULAR_FILE, parent);
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

  @Override
  protected void release() {
    bytes = null;
    size = -1;
  }
}
