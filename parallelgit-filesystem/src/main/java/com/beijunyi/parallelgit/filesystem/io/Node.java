package com.beijunyi.parallelgit.filesystem.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

public abstract class Node {

  protected final DirectoryNode parent;
  protected volatile FileMode mode;
  protected volatile AnyObjectId object;
  protected volatile boolean dirty = false;
  protected volatile boolean deleted = false;

  protected Node(@Nonnull FileMode mode, @Nullable AnyObjectId object, @Nullable DirectoryNode parent) {
    this.mode = mode;
    this.object = object;
    this.parent = parent;
  }

  @Nonnull
  public static Node forObject(@Nonnull AnyObjectId object, @Nonnull FileMode mode, @Nonnull DirectoryNode parent) {
    if(mode.equals(FileMode.TREE))
      return DirectoryNode.forTreeObject(object, parent);
    return FileNode.forBlobObject(object, mode, parent);
  }

  @Nonnull
  public static Node cloneNode(@Nonnull Node node, @Nonnull DirectoryNode parent) {
    Node ret;
    if(node instanceof DirectoryNode)
      ret = DirectoryNode.newDirectory(parent);
    else
      ret = FileNode.newFile(node.isExecutableFile(), parent);
    ret.setDirty(true);
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
    return mode == FileMode.REGULAR_FILE || mode == FileMode.EXECUTABLE_FILE;
  }

  public boolean isExecutableFile() {
    return mode == FileMode.EXECUTABLE_FILE;
  }

  public boolean isSymbolicLink() {
    return mode == FileMode.SYMLINK;
  }

  public boolean isDirectory() {
    return mode == FileMode.TREE;
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

  public boolean isDirty() {
    return dirty;
  }

  public void setDirty(boolean dirty) {
    this.dirty = dirty;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public void reset(@Nonnull AnyObjectId object, @Nonnull FileMode mode) {
    release();
    if(!object.equals(this.object) || !mode.equals(this.mode)) {
      setObject(object);
      setMode(mode);
      markDirty();
    }
  }

  public void markDirty() {
    if(!deleted && !isDirty()) {
      setDirty(true);
      DirectoryNode parent = getParent();
      if(parent != null)
        parent.markDirty();
    }
  }

  public void markClean(@Nullable AnyObjectId object) {
    if(!deleted) {
      setObject(object);
      setDirty(false);
    }
  }

  public void markDeleted() {
    setDeleted(true);
    release();
  }

  protected abstract void release();

}
