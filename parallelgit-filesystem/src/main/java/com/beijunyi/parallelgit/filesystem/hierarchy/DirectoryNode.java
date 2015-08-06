package com.beijunyi.parallelgit.filesystem.hierarchy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;

public class DirectoryNode extends Node {

  protected Map<String, Node> children;

  protected DirectoryNode(@Nonnull AnyObjectId object) {
    super(NodeType.DIRECTORY, object);
  }

  protected DirectoryNode() {
    this(ObjectId.zeroId());
    children = new HashMap<>();
    dirty = true;
  }

  @Nonnull
  public static DirectoryNode forTreeObject(@Nonnull AnyObjectId object) {
    return new DirectoryNode(object);
  }

  @Nonnull
  public static DirectoryNode newDirectory() {
    return new DirectoryNode();
  }

  @Nonnull
  public Set<String> getChildrenNames() {
    return children.keySet();
  }

  @Nullable
  public Map<String, Node> getChildren() {
    return children;
  }

  public void setChildren(@Nullable Map<String, Node> children) {
    this.children = children;
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
    return true;
  }

  public synchronized boolean removeChild(@Nonnull String name) {
    return children.remove(name) != null;
  }

}
