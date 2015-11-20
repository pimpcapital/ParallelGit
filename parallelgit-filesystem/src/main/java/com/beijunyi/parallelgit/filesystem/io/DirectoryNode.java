package com.beijunyi.parallelgit.filesystem.io;

import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsDataService;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

public class DirectoryNode extends Node {

  private ConcurrentMap<String, Node> children;

  protected DirectoryNode(@Nullable AnyObjectId object, @Nonnull GfsDataService ds) {
    super(FileMode.TREE, object, ds);
  }

  protected DirectoryNode(@Nonnull GfsDataService ds) {
    this(null, ds);
  }

  @Nonnull
  public static DirectoryNode forTreeObject(@Nonnull AnyObjectId object, @Nonnull GfsDataService ds) {
    return new DirectoryNode(object, ds);
  }

  @Nonnull
  public static DirectoryNode newDirectory(@Nonnull GfsDataService ds) {
    return new DirectoryNode(ds);
  }

  @Nullable
  public ConcurrentMap<String, Node> getChildren() {
    return children;
  }

  public void setChildren(@Nullable ConcurrentMap<String, Node> children) {
    this.children = children;
  }

  public boolean isEmpty() {
    if(children != null)
      return children.isEmpty();
    return object == null;
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
    return true;
  }

  public synchronized boolean removeChild(@Nonnull String name) {
    Node removed = children.remove(name);
    if(removed == null)
      return false;
    removed.markDeleted();
    return true;
  }

  @Override
  public boolean isInitialized() {
    return children != null;
  }

  @Override
  protected boolean isTrivial() {
    return isEmpty();
  }

  @Override
  protected void reset() {
    if(children != null) {
      for(Node child : children.values())
        child.markDeleted();
      children = null;
    }
  }

  @Override
  public void takeSnapshot() {
  }

}
