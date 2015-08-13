package com.beijunyi.parallelgit.filesystem.io;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;

public class DirectoryNode extends Node {

  protected Map<String, Node> children;

  protected DirectoryNode(@Nonnull AnyObjectId object, @Nullable DirectoryNode parent) {
    super(NodeType.DIRECTORY, object, parent);
  }

  protected DirectoryNode(@Nullable DirectoryNode parent) {
    this(ObjectId.zeroId(), parent);
    dirty = true;
  }

  @Nonnull
  public static DirectoryNode forTreeObject(@Nonnull AnyObjectId object, @Nonnull DirectoryNode parent) {
    return new DirectoryNode(object, parent);
  }

  @Nonnull
  public static DirectoryNode newDirectory(@Nonnull DirectoryNode parent) {
    return new DirectoryNode(parent);
  }

  @Nonnull
  public static DirectoryNode treeRoot(@Nonnull AnyObjectId object) {
    return new DirectoryNode(object, null);
  }

  @Nonnull
  public static DirectoryNode emptyRoot() {
    return new DirectoryNode(null);
  }


  @Nullable
  public Map<String, Node> getChildren() {
    return children;
  }

  public void setChildren(@Nullable Map<String, Node> children) {
    this.children = children;
  }

  public void loadChildren(@Nonnull Map<String, Node> children) {
    setChildren(children);
  }

  public boolean hasChild(@Nonnull String name) {
    if(children == null)
      throw new IllegalStateException();
    return children.containsKey(name);
  }

  @Nullable
  public Node getChild(@Nonnull String name) {
    if(children == null)
      throw new IllegalStateException();
    return children.get(name);
  }

  public synchronized boolean addChild(@Nonnull String name, @Nonnull Node child, boolean replace) {
    if(!replace && children.containsKey(name))
      return false;
    children.put(name, child);
    markDirty();
    return true;
  }

  public synchronized boolean removeChild(@Nonnull String name) {
    if(children.remove(name) != null) {
      markDirty();
      return true;
    }
    return false;
  }

}
