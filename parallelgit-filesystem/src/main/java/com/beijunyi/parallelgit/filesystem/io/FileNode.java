package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsObjectService;
import com.beijunyi.parallelgit.utils.io.BlobSnapshot;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

import static org.eclipse.jgit.lib.FileMode.*;

public class FileNode extends Node<BlobSnapshot> {

  private FileMode mode;
  private byte[] bytes;
  private long size = -1;

  private FileNode(@Nullable AnyObjectId id, @Nonnull FileMode mode, @Nonnull GfsObjectService objService) {
    super(id, objService);
    this.mode = mode;
  }

  @Nonnull
  protected static FileNode fromObject(@Nonnull AnyObjectId id, @Nonnull FileMode mode, @Nonnull GfsObjectService objService) {
    return new FileNode(id, mode, objService);
  }

  @Nonnull
  public static FileNode fromBytes(@Nonnull byte[] bytes, @Nonnull FileMode mode, @Nonnull GfsObjectService objService) {
    FileNode ret = new FileNode(null, mode, objService);
    ret.bytes = bytes;
    return ret;
  }

  @Nonnull
  public static FileNode newFile(boolean executable, @Nonnull GfsObjectService objService) {
    return new FileNode(null, executable ? EXECUTABLE_FILE : REGULAR_FILE, objService);
  }

  public long getSize() throws IOException {
    if(size != -1)
      return size;
    size = id != null ? objService.getBlobSize(id) : 0;
    return size;
  }

  @Nonnull
  @Override
  public FileMode getMode() {
    return mode;
  }

  @Override
  public void setMode(@Nonnull FileMode mode) {
    if(mode.equals(TREE) || mode.equals(GITLINK))
      throw new IllegalArgumentException(mode.toString());
    this.mode = mode;
  }

  @Override
  public synchronized void reset(@Nonnull AnyObjectId id) {
    this.id = id;
    bytes = null;
  }

  @Nullable
  @Override
  public BlobSnapshot loadSnapshot() throws IOException {
    return id != null ? objService.readBlob(id) : null;
  }

  @Nullable
  @Override
  public BlobSnapshot takeSnapshot(boolean persist, boolean allowEmpty) throws IOException {
    if(bytes == null)
      return null;
    BlobSnapshot ret = BlobSnapshot.capture(bytes);
    if(persist)
      id = objService.write(ret);
    return ret;
  }

  @Override
  public boolean isInitialized() {
    return bytes != null;
  }

  @Nonnull
  @Override
  public Node clone(@Nonnull GfsObjectService targetObjService) throws IOException {
    FileNode ret = new FileNode(null, mode, targetObjService);
    if(bytes != null)
      ret.bytes = bytes;
    else {
      ret.id = id;
      targetObjService.pullObject(id, objService);
    }
    return ret;
  }

  @Nonnull
  public byte[] getBytes() throws IOException {
    if(bytes != null)
      return bytes;
    initBytes();
    return bytes;
  }

  public void setBytes(@Nullable byte[] bytes) {
    this.bytes = bytes;
    this.size = bytes != null ? bytes.length : -1;
  }

  private synchronized void initBytes() throws IOException {
    if(bytes == null) {
      BlobSnapshot snapshot = loadSnapshot();
      bytes = snapshot != null ? snapshot.getBytes() : new byte[0];
      size = bytes.length;
    }
  }

}
