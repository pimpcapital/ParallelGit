package com.beijunyi.parallelgit.filesystem.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

public class FileNode extends Node {

  private byte[] bytes;
  private long size = -1;

  private FileNode(@Nonnull FileMode mode, @Nullable AnyObjectId object, @Nonnull GitFileSystem gfs) {
    super(mode, object, gfs);
  }

  private FileNode(@Nonnull FileMode mode, @Nonnull GitFileSystem gfs) {
    this(mode, null, gfs);
  }

  @Nonnull
  protected static FileNode forBlobObject(@Nonnull AnyObjectId object, @Nonnull FileMode mode, @Nonnull GitFileSystem gfs) {
    return new FileNode(mode, object, gfs);
  }

  @Nonnull
  public static FileNode forBytes(@Nonnull byte[] bytes, @Nonnull FileMode mode, @Nonnull GitFileSystem gfs) {
    FileNode ret = new FileNode(mode, gfs);
    ret.setBytes(bytes);
    return ret;
  }

  @Nonnull
  public static FileNode newFile(boolean executable, @Nonnull GitFileSystem gfs) {
    return new FileNode(executable ? FileMode.EXECUTABLE_FILE : FileMode.REGULAR_FILE, gfs);
  }

  @Nullable
  public byte[] getBytes() {
    return bytes;
  }

  public void setBytes(@Nullable byte[] bytes) {
    this.bytes = bytes;
    this.size = bytes != null ? bytes.length : -1;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  @Override
  protected boolean isTrivial() {
    return false;
  }

  @Override
  protected void reset() {
    setBytes(null);
  }

}
