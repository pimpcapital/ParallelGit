package com.beijunyi.parallelgit.filesystem.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

public abstract class Node {

  protected final NodeType type;
  protected final DirectoryNode parent;
  protected volatile AnyObjectId object;
  protected volatile boolean dirty = false;

  protected Node(@Nonnull NodeType type, @Nonnull AnyObjectId object, @Nullable DirectoryNode parent) {
    this.type = type;
    this.object = object;
    this.parent = parent;
  }

  @Nonnull
  public static Node forObject(@Nonnull AnyObjectId object, @Nonnull FileMode mode, @Nonnull DirectoryNode parent) {
    if(mode.equals(FileMode.TREE))
      return DirectoryNode.forTreeObject(object, parent);
    return FileNode.forBlobObject(object, NodeType.forFileMode(mode), parent);
  }

  @Nonnull
  public static Node ofSameType(@Nonnull Node node, @Nonnull DirectoryNode parent) {
    if(node instanceof DirectoryNode)
      return DirectoryNode.newDirectory(parent);
    return FileNode.newFile(node.isExecutableFile(), parent);
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

  @Nullable
  public DirectoryNode getParent() {
    return parent;
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

}
