package com.beijunyi.parallelgit.filesystem.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsDataService;
import com.beijunyi.parallelgit.utils.io.BlobSnapshot;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

import static org.eclipse.jgit.lib.FileMode.*;

public class FileNode extends Node<BlobSnapshot> {

  private FileMode mode;
  private byte[] bytes;
  private long size = -1;

  private FileNode(@Nullable AnyObjectId id, @Nullable BlobSnapshot snapshot, @Nonnull FileMode mode, @Nonnull GfsDataService gds) {
    super(id, snapshot, gds);
    this.mode = mode;
  }

  @Nonnull
  protected static FileNode forBlobId(@Nonnull AnyObjectId object, @Nonnull FileMode mode, @Nonnull GfsDataService gds) {
    return new FileNode(mode, object, gds);
  }

  @Nonnull
  public static FileNode newFile(@Nonnull byte[] bytes, @Nonnull FileMode mode, @Nonnull GfsDataService gds) {
    FileNode ret = new FileNode(null, mode, gds);
    ret.bytes = bytes;
    return ret;
  }

  @Nonnull
  public static FileNode newFile(boolean executable, @Nonnull GfsDataService gds) {
    return new FileNode(null, executable ? EXECUTABLE_FILE : REGULAR_FILE, gds);
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
