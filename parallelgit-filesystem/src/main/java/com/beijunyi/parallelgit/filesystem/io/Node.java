package com.beijunyi.parallelgit.filesystem.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

import static org.eclipse.jgit.lib.FileMode.*;

public abstract class Node {

  protected final GitFileSystem gfs;
  protected volatile FileMode mode;
  protected volatile AnyObjectId object;
  protected volatile AnyObjectId snapshot;
  protected volatile boolean deleted = false;

  protected Node(@Nonnull FileMode mode, @Nullable AnyObjectId object, @Nonnull GitFileSystem gfs) {
    this.mode = mode;
    this.object = object;
    this.gfs = gfs;
  }

  @Nonnull
  public static Node forObject(@Nonnull AnyObjectId object, @Nonnull FileMode mode, @Nonnull GitFileSystem gfs) {
    if(mode.equals(TREE))
      return DirectoryNode.forTreeObject(object, gfs);
    return FileNode.forBlobObject(object, mode, gfs);
  }

  @Nonnull
  public static Node cloneNode(@Nonnull Node node, @Nonnull GitFileSystem gfs) {
    Node ret;
    if(node instanceof DirectoryNode)
      ret = DirectoryNode.newDirectory(gfs);
    else
      ret = FileNode.newFile(node.isExecutableFile(), gfs);
    return ret;
  }

  @Nonnull
  public FileMode getMode() {
    return mode;
  }

  public void setMode(@Nonnull FileMode mode) {
    this.mode = mode;
  }

  public boolean isRegularFile() {
    return mode.equals(REGULAR_FILE) || mode.equals(EXECUTABLE_FILE);
  }

  public boolean isExecutableFile() {
    return mode.equals(EXECUTABLE_FILE);
  }

  public boolean isSymbolicLink() {
    return mode.equals(FileMode.SYMLINK);
  }

  public boolean isDirectory() {
    return mode.equals(TREE);
  }

  @Nullable
  public DirectoryNode getParent() {
    return parent;
  }

  @Nullable
  public AnyObjectId getObject() {
    return object;
  }

  public void setObject(@Nullable AnyObjectId object) {
    this.object = object;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public void reset(@Nonnull AnyObjectId newId, @Nonnull FileMode newMode) {
    if((mode.equals(TREE) || newMode.equals(TREE)) && !mode.equals(newMode))
      throw new IllegalStateException();
    reset();
    if(!newId.equals(object) || !newMode.equals(mode)) {
      setObject(newId);
      setMode(newMode);
    }
  }

  public void markDeleted() {
    setDeleted(true);
    reset();
  }

  public void takeSnapshot() {
    snapshot = Snapshot.capture(this);
  }

  public boolean isDirty() {
    return snapshot == null || snapshot.matches(this);
  }

  protected abstract boolean isTrivial();

  protected abstract void reset();

}
