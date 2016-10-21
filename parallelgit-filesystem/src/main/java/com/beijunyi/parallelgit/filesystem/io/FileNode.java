package com.beijunyi.parallelgit.filesystem.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.exceptions.IncompatibleFileModeException;
import com.beijunyi.parallelgit.utils.io.BlobSnapshot;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import static org.eclipse.jgit.lib.FileMode.*;

public class FileNode extends Node<BlobSnapshot, byte[]> {

  private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

  private long size = -1;

  private FileNode(ObjectId id, FileMode mode, DirectoryNode parent) {
    super(id, mode, parent);
  }

  private FileNode(FileMode mode, DirectoryNode parent) {
    super(mode, parent);
  }

  private FileNode(byte[] bytes, FileMode mode, DirectoryNode parent) {
    super(bytes, mode, parent);
  }

  @Nonnull
  protected static FileNode fromBlob(ObjectId id, FileMode mode, DirectoryNode parent) {
    return new FileNode(id, mode, parent);
  }

  @Nonnull
  public static FileNode fromBytes(byte[] bytes, FileMode mode, DirectoryNode parent) {
    return new FileNode(bytes, mode, parent);
  }

  @Nonnull
  public static FileNode newFile(FileMode mode, DirectoryNode parent) {
    return new FileNode(mode, parent);
  }

  @Nonnull
  public static FileNode newFile(boolean executable, DirectoryNode parent) {
    return newFile(executable ? EXECUTABLE_FILE : REGULAR_FILE, parent);
  }

  @Override
  protected Class<? extends BlobSnapshot> getSnapshotType() {
    return BlobSnapshot.class;
  }

  public long getSize() throws IOException {
    if(size != -1)
      return size;
    size = id != null ? objService.getBlobSize(id) : 0;
    return size;
  }

  @Nonnull
  @Override
  protected byte[] getDefaultData() {
    return EMPTY_BYTE_ARRAY;
  }

  @Nonnull
  public InputStream getInputStream() throws IOException {
    if (id == null && data == null)
        data = new byte[0];
    if(data != null) {
      return new ByteArrayInputStream(data);
    }
    BlobSnapshot snapshot = loadSnapshot(id);
    return snapshot.getInputStream();
  }


  @Nonnull
  @Override
  protected byte[] loadData(BlobSnapshot snapshot) throws IOException {
    return snapshot.getData();
  }

  @Override
  protected boolean isTrivial(byte[] data) {
    return false;
  }

  @Nonnull
  @Override
  protected BlobSnapshot captureData(byte[] data, boolean persist) {
    return BlobSnapshot.capture(data);
  }

  @Nonnull
  @Override
  public Node clone(DirectoryNode parent) throws IOException {
    FileNode ret;
    if(isInitialized()) {
      ret = newFile(mode, parent);
      ret.data = data;
      ret.size = data.length;
    } else if(id != null) {
      ret = FileNode.fromBlob(id , mode, parent);
      parent.getObjectService().pullObject(id, objService);
    } else
      throw new IllegalStateException();
    return ret;
  }

  public void setBytes(byte[] bytes) {
    this.data = bytes;
    this.size = bytes.length;
    id = null;
    invalidateParentCache();
  }

  protected void checkFileMode(FileMode proposed) {
    if(TREE.equals(proposed) || GITLINK.equals(proposed))
      throw new IncompatibleFileModeException(mode, proposed);
  }

}
