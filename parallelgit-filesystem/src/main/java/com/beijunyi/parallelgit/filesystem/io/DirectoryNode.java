package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsDataService;
import com.beijunyi.parallelgit.utils.io.GitFileEntry;
import com.beijunyi.parallelgit.utils.io.TreeSnapshot;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

import static org.eclipse.jgit.lib.FileMode.TREE;

public class DirectoryNode extends Node<TreeSnapshot> {

  private ConcurrentMap<String, Node> children;

  protected DirectoryNode(@Nullable AnyObjectId id, @Nullable TreeSnapshot snapshot, @Nonnull GfsDataService gds) {
    super(null, snapshot, gds);
  }

  @Nonnull
  public static DirectoryNode fromObject(@Nonnull AnyObjectId id, @Nonnull GfsDataService gds) {
    return new DirectoryNode(id, null, gds);
  }

  @Nonnull
  public static DirectoryNode fromSnapshot(@Nonnull TreeSnapshot snapshot, @Nonnull GfsDataService gds) {
    return new DirectoryNode(null, snapshot, gds);
  }

  @Nonnull
  public static DirectoryNode newDirectory(@Nonnull GfsDataService gds) {
    return new DirectoryNode(null, gds);
  }

  @Nonnull
  public ConcurrentMap<String, Node> getChildren() throws IOException {
    if(children != null)
      return children;
    snapshot = loadSnapshot();
    children = extractSnapshot(snapshot);
    return children;
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

  @Nonnull
  @Override
  public FileMode getMode() {
    return TREE;
  }

  @Nonnull
  @Override
  public TreeSnapshot loadSnapshot() throws IOException {
    if(id == null)
      throw new IllegalStateException();
    return gds.readTree(id);
  }

  @Nullable
  @Override
  public TreeSnapshot takeSnapshot() throws IOException {
    if(children == null)
      return null;
    Map<String, GitFileEntry> entries = new HashMap<>();
    for(Map.Entry<String, Node> child : children.entrySet()) {
      Node node = child.getValue();
      AnyObjectId id = node.persist();
      if(id != null)
        entries.put(child.getKey(), new GitFileEntry(id, node.getMode()));
    }
    return TreeSnapshot.capture(entries);
  }

  @Nonnull
  private ConcurrentMap<String, Node> extractSnapshot(@Nonnull TreeSnapshot snapshot) {
    ConcurrentMap<String, Node> ret = new ConcurrentHashMap<>();
    for(Map.Entry<String, GitFileEntry> entry : snapshot.getChildren().entrySet()) {
      ret.put(entry.getKey(), Node.fromEntry(entry.getValue(), gds));
    }
    return ret;
  }

}
