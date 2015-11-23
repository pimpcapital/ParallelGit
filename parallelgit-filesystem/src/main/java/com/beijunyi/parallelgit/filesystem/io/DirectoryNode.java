package com.beijunyi.parallelgit.filesystem.io;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsDataService;
import com.beijunyi.parallelgit.utils.io.GitFileEntry;
import com.beijunyi.parallelgit.utils.io.TreeSnapshot;
import org.eclipse.jgit.lib.AnyObjectId;

public class DirectoryNode extends Node<TreeSnapshot> {

  private ConcurrentMap<String, Node> children;

  protected DirectoryNode(@Nullable TreeSnapshot snapshot, @Nonnull GfsDataService ds) {
    super(snapshot, ds);
  }

  @Nonnull
  public static DirectoryNode fromObject(@Nonnull AnyObjectId id, )

  @Nonnull
  public static DirectoryNode fromSnapshot(@Nonnull TreeSnapshot snapshot, @Nonnull GfsDataService gds) {
    return new DirectoryNode(snapshot, gds);
  }

  @Nonnull
  public static DirectoryNode newDirectory(@Nonnull GfsDataService gds) {
    return new DirectoryNode(null, gds);
  }

  @Nullable
  public ConcurrentMap<String, Node> getChildren() {
    if(children != null)
      loadChildren();
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

  private synchronized void loadChildren() {
    if(children == null) {
      children = new ConcurrentHashMap<>();
      if(snapshot != null) {
        for(Map.Entry<String, GitFileEntry> entry : snapshot.getChildren().entrySet())
          children.put(entry.getKey(), Node.fromEntry(entry.getValue(), gds));
      }
    }
  }

}
