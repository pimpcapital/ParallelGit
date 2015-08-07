package com.beijunyi.parallelgit.filesystem.hierarchy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;

public class FileNode extends Node {

  private byte[] bytes;

  private FileNode(@Nonnull NodeType type, @Nonnull AnyObjectId object) {
    super(type, object);
  }

  private FileNode(@Nonnull NodeType type) {
    this(type, ObjectId.zeroId());
    dirty = true;
  }

  @Nonnull
  protected static FileNode forBlobObject(@Nonnull AnyObjectId object, @Nonnull NodeType type) {
    return new FileNode(type, object);
  }

  @Nonnull
  public static FileNode newFile(boolean executable) {
    return new FileNode(executable ? NodeType.EXECUTABLE_FILE : NodeType.NON_EXECUTABLE_FILE);
  }

  @Nullable
  public byte[] getBytes() {
    return bytes;
  }

  public void setBytes(@Nullable byte[] bytes) {
    this.bytes = bytes;
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
