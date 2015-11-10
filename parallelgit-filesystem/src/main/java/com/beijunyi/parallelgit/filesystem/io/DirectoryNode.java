package com.beijunyi.parallelgit.filesystem.io;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;

public class DirectoryNode extends Node {

  protected Map<String, Node> children;

  protected DirectoryNode(@Nullable AnyObjectId object, @Nullable DirectoryNode parent) {
    super(NodeType.DIRECTORY, object, parent);
  }

  protected DirectoryNode(@Nullable DirectoryNode parent) {
    this(null, parent);
    dirty = false;
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
    DirectoryNode ret = new DirectoryNode(null);
    ret.setDirty(true);
    return ret;
  }

  @Nullable
  public Map<String, Node> getChildren() {
    return children;
  }

  public void setChildren(@Nullable Map<String, Node> children) {
    this.children = children;
  }

  public boolean isEmpty() {
    if(children != null)
      return children.isEmpty();
    return object == null;
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
    if(child.isDirty() || !child.isDirectory() || !((DirectoryNode) child).isEmpty())
      markDirty();
    return true;
  }

  public synchronized boolean removeChild(@Nonnull String name) {
    Node removed = children.remove(name);
    if(removed == null)
      return false;
    removed.markDeleted();
    if(!removed.isDirectory() || !((DirectoryNode) removed).isEmpty())
      markDirty();
    return true;
  }

  @Override
  public void markDeleted() {
    super.markDeleted();
    if(children != null) {
      for(Node child : children.values())
        child.markDeleted();
      children = null;
    }
  }

}
