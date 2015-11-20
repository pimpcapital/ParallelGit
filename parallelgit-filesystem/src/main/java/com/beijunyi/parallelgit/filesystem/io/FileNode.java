package com.beijunyi.parallelgit.filesystem.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsDataService;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

public class FileNode extends Node {

  private byte[] bytes;
  private long size = -1;

  private FileNode(@Nonnull FileMode mode, @Nullable AnyObjectId object, @Nonnull GfsDataService ds) {
    super(mode, object, ds);
  }

  private FileNode(@Nonnull FileMode mode, @Nonnull GfsDataService ds) {
    this(mode, null, ds);
  }

  @Nonnull
  protected static FileNode forBlobObject(@Nonnull AnyObjectId object, @Nonnull FileMode mode, @Nonnull GfsDataService ds) {
    return new FileNode(mode, object, ds);
  }

  @Nonnull
  public static FileNode newFile(@Nonnull byte[] bytes, @Nonnull FileMode mode, @Nonnull GfsDataService ds) {
    FileNode ret = new FileNode(mode, ds);
    ret.bytes = bytes;
    return ret;
  }

  @Nonnull
  public static FileNode newFile(boolean executable, @Nonnull GfsDataService ds) {
    return new FileNode(executable ? FileMode.EXECUTABLE_FILE : FileMode.REGULAR_FILE, ds);
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
  public boolean isInitialized() {
    return bytes != null;
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
