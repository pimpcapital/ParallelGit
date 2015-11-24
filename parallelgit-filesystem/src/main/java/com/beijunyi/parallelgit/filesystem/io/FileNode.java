package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
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
  protected static FileNode fromObject(@Nonnull AnyObjectId id, @Nonnull FileMode mode, @Nonnull GfsDataService gds) {
    return new FileNode(id, null, mode, gds);
  }

  @Nonnull
  public static FileNode newFile(@Nonnull byte[] bytes, @Nonnull FileMode mode, @Nonnull GfsDataService gds) {
    FileNode ret = new FileNode(null, null, mode, gds);
    ret.bytes = bytes;
    return ret;
  }

  @Nonnull
  public static FileNode newFile(boolean executable, @Nonnull GfsDataService gds) {
    return new FileNode(null, null, executable ? EXECUTABLE_FILE : REGULAR_FILE, gds);
  }

  @Nonnull
  public byte[] getBytes() throws IOException {
    if(bytes != null)
      return bytes;
    snapshot = loadSnapshot();
    bytes = snapshot.getBytes();
    return bytes;
  }

  public void setBytes(@Nullable byte[] bytes) {
    this.bytes = bytes;
    this.size = bytes != null ? bytes.length : -1;
  }

  public long getSize() throws IOException {
    if(size != -1)
      return size;
    size = gds.getBlobSize(id);
    return size;
  }

  @Nonnull
  @Override
  public FileMode getMode() {
    return mode;
  }

  @Nonnull
  @Override
  public BlobSnapshot loadSnapshot() throws IOException {
    if(id == null)
      throw new IllegalStateException();
    return gds.readBlob(id);
  }

  @Nullable
  @Override
  public BlobSnapshot takeSnapshot() {
    return bytes != null ? BlobSnapshot.capture(bytes) : null;
  }
}
