package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsDataProvider;
import com.beijunyi.parallelgit.utils.io.GitFileEntry;
import com.beijunyi.parallelgit.utils.io.ObjectSnapshot;
import com.beijunyi.parallelgit.utils.io.TreeSnapshot;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

import static org.eclipse.jgit.lib.FileMode.TREE;

public class DirectoryNode extends Node<TreeSnapshot> {

  private ConcurrentMap<String, Node> children;

  protected DirectoryNode(@Nullable AnyObjectId id, @Nonnull GfsDataProvider gds) {
    super(id, gds);
  }

  @Nonnull
  public static DirectoryNode fromObject(@Nonnull AnyObjectId id, @Nonnull GfsDataProvider gds) {
    return new DirectoryNode(id, gds);
  }

  @Nonnull
  public static DirectoryNode newDirectory(@Nonnull GfsDataProvider gds) {
    return new DirectoryNode(null, gds);
  }

  @Override
  public long getSize() throws IOException {
    return 0;
  }

  @Nonnull
  @Override
  public FileMode getMode() {
    return TREE;
  }

  @Override
  public void setMode(@Nonnull FileMode mode) {
    if(mode.equals(TREE))
      throw new IllegalArgumentException(mode.toString());
  }

  @Nullable
  @Override
  public TreeSnapshot loadSnapshot() throws IOException {
    return id != null ? gds.readTree(id) : null;
  }

  @Nullable
  @Override
  public TreeSnapshot takeSnapshot(boolean persist, boolean allowEmpty) throws IOException {
    if(children == null)
      return null;
    Map<String, GitFileEntry> entries = new HashMap<>();
    for(Map.Entry<String, Node> child : children.entrySet()) {
      Node node = child.getValue();
      ObjectSnapshot snapshot = node.takeSnapshot(persist, false);
      if(snapshot != null)
        entries.put(child.getKey(), new GitFileEntry(snapshot.getId(), node.getMode()));
    }
    TreeSnapshot ret = TreeSnapshot.capture(entries, false);
    if(persist)
      id = ret != null ? gds.write(ret) : null;
    return ret;
  }

  @Nonnull
  @Override
  public Node clone(@Nonnull GfsDataProvider targetGds) throws IOException {
    DirectoryNode ret = new DirectoryNode(null, targetGds);
    if(children != null) {
      for(Map.Entry<String, Node> child : children.entrySet()) {
        String name = child.getKey();
        Node node = child.getValue();
        ret.addChild(name, node.clone(targetGds), false);
      }
    } else {
      ret.id = id;
      targetGds.pullObject(id, gds);
    }
    return ret;
  }

  @Nonnull
  public ConcurrentMap<String, Node> getChildren() throws IOException {
    initChildren();
    return children;
  }

  public boolean hasChild(@Nonnull String name) throws IOException {
    initChildren();
    return children.containsKey(name);
  }

  @Nullable
  public Node getChild(@Nonnull String name) throws IOException {
    initChildren();
    return children.get(name);
  }

  public synchronized boolean addChild(@Nonnull String name, @Nonnull Node child, boolean replace) throws IOException {
    initChildren();
    if(!replace && children.containsKey(name))
      return false;
    children.put(name, child);
    return true;
  }

  public synchronized boolean removeChild(@Nonnull String name) {
    Node removed = children.remove(name);
    return removed != null;
  }

  private synchronized void initChildren() throws IOException {
    if(children == null) {
      TreeSnapshot tree = loadSnapshot();
      children = new ConcurrentHashMap<>();
      if(tree != null)
        for(Map.Entry<String, GitFileEntry> entry : tree.getChildren().entrySet())
          children.put(entry.getKey(), Node.fromEntry(entry.getValue(), gds));
    }
  }

}
