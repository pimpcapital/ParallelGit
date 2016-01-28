package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.io.BlobSnapshot;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

import static org.eclipse.jgit.lib.FileMode.*;

public class FileNode extends Node<BlobSnapshot> {

  private FileMode mode;
  private byte[] bytes;
  private long size = -1;

  private FileNode(@Nullable AnyObjectId id, @Nonnull FileMode mode, @Nonnull DirectoryNode parent) {
    super(id, parent);
    this.mode = mode;
    if(id == null)
      bytes = new byte[0];
  }

  @Nonnull
  protected static FileNode fromObject(@Nonnull AnyObjectId id, @Nonnull FileMode mode, @Nonnull DirectoryNode parent) {
    return new FileNode(id, mode, parent);
  }

  @Nonnull
  public static FileNode fromBytes(@Nonnull byte[] bytes, @Nonnull FileMode mode, @Nonnull DirectoryNode parent) {
    FileNode ret = new FileNode(null, mode, parent);
    ret.bytes = bytes;
    return ret;
  }

  @Nonnull
  public static FileNode newFile(@Nonnull FileMode mode, @Nonnull DirectoryNode parent) {
    return new FileNode(null, mode, parent);
  }

  @Nonnull
  public static FileNode newFile(boolean executable, @Nonnull DirectoryNode parent) {
    return newFile(executable ? EXECUTABLE_FILE : REGULAR_FILE, parent);
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
    id = null;
    propagateChange();
  }

  @Override
  public synchronized void reset(@Nonnull AnyObjectId id) {
    this.id = id;
    bytes = null;
    propagateChange();
  }

  @Nullable
  @Override
  public BlobSnapshot loadSnapshot() throws IOException {
    return id != null ? objService.readBlob(id) : null;
  }

  @Nullable
  @Override
  public BlobSnapshot takeSnapshot(boolean persist) throws IOException {
    if(bytes == null)
      return null;
    BlobSnapshot ret = BlobSnapshot.capture(bytes);
    if(persist)
      objService.write(ret);
    return ret;
  }

  @Override
  public boolean isInitialized() {
    return bytes != null;
  }

  @Nonnull
  @Override
  public Node clone(@Nonnull DirectoryNode parent) throws IOException {
    FileNode ret = newFile(mode, parent);
    if(bytes != null)
      ret.bytes = bytes;
    else {
      ret.reset(id);
      parent.getObjService().pullObject(id, objService);
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

  public void setBytes(@Nonnull byte[] bytes) {
    this.bytes = bytes;
    this.size = bytes.length;
    id = null;
    propagateChange();
  }

  private synchronized void initBytes() throws IOException {
    if(bytes == null) {
      BlobSnapshot snapshot = loadSnapshot();
      bytes = snapshot != null ? snapshot.getBytes() : new byte[0];
      size = bytes.length;
    }
  }

}
