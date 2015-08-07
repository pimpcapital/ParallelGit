package com.beijunyi.parallelgit.filesystem.hierarchy;

import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

public abstract class Node {

  protected final NodeType type;
  protected volatile AnyObjectId object;
  protected volatile boolean dirty = false;
  protected volatile long size = -1;

  protected Node(@Nonnull NodeType type, @Nonnull AnyObjectId object) {
    this.type = type;
    this.object = object;
  }

  @Nonnull
  public static Node forObject(@Nonnull AnyObjectId object, @Nonnull FileMode mode) {
    if(mode.equals(FileMode.TREE))
      return DirectoryNode.forTreeObject(object);
    return FileNode.forBlobObject(object, NodeType.forFileMode(mode));
  }

  @Nonnull
  public static Node ofSameType(@Nonnull Node node) {
    if(node instanceof DirectoryNode)
      return DirectoryNode.newDirectory();
    return FileNode.newFile(node.isExecutableFile());
  }

  @Nonnull
  public NodeType getType() {
    return type;
  }

  public boolean isRegularFile() {
    return type.isRegularFile();
  }

  public boolean isExecutableFile() {
    return type == NodeType.EXECUTABLE_FILE;
  }

  public boolean isSymbolicLink() {
    return type == NodeType.SYMBOLIC_LINK;
  }

  public boolean isDirectory() {
    return type == NodeType.DIRECTORY;
  }

  @Nonnull
  public AnyObjectId getObject() {
    return object;
  }

  public void setObject(@Nonnull AnyObjectId object) {
    this.object = object;
  }

  public boolean isDirty() {
    return dirty;
  }

  public void setDirty(boolean dirty) {
    this.dirty = dirty;
  }

  public void markDirty() {
    setDirty(true);
  }

  public void markClean(@Nonnull AnyObjectId object) {
    setObject(object);
    setDirty(false);
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public void unsetSize() {
    setSize(-1L);
  }

}
