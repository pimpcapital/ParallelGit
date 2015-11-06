package com.beijunyi.parallelgit.filesystem.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

public abstract class Node {

  protected NodeType type;
  protected final DirectoryNode parent;
  protected volatile AnyObjectId object;
  protected volatile boolean dirty = false;

  protected Node(@Nonnull NodeType type, @Nullable AnyObjectId object, @Nullable DirectoryNode parent) {
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
  public static FileNode forBytes(@Nonnull byte[] bytes, @Nonnull FileMode mode, @Nonnull DirectoryNode parent) {
    return FileNode.forBytes(bytes, NodeType.forFileMode(mode), parent);
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
  public NodeType getType() {
    return type;
  }

  public void setType(@Nonnull NodeType type) {
    this.type = type;
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

  @Nullable
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
    if(!isDirty()) {
      setDirty(true);
      DirectoryNode parent = getParent();
      if(parent != null)
        parent.markDirty();
    }
  }

  public void markClean(@Nonnull AnyObjectId object) {
    setObject(object);
    setDirty(false);
  }

}
