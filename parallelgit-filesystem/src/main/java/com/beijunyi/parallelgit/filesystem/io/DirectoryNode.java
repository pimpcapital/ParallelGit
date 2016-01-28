package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsObjectService;
import com.beijunyi.parallelgit.utils.io.GitFileEntry;
import com.beijunyi.parallelgit.utils.io.TreeSnapshot;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

import static org.eclipse.jgit.lib.FileMode.TREE;

public class DirectoryNode extends Node<TreeSnapshot> {

  private ConcurrentMap<String, Node> children;

  protected DirectoryNode(@Nullable AnyObjectId id, @Nonnull GfsObjectService objService) {
    super(id, objService);
    if(id == null)
      setupEmptyDirectory();
  }

  protected DirectoryNode(@Nullable AnyObjectId id, @Nonnull DirectoryNode parent) {
    super(id, parent);
    if(id == null)
      setupEmptyDirectory();
  }

  @Nonnull
  public static DirectoryNode fromObject(@Nonnull AnyObjectId id, @Nonnull DirectoryNode parent) {
    return new DirectoryNode(id, parent);
  }

  @Nonnull
  public static DirectoryNode newDirectory(@Nonnull DirectoryNode parent) {
    return new DirectoryNode(null, parent);
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

  @Override
  public synchronized void reset(@Nonnull AnyObjectId id) {
    this.id = id;
    children = null;
    propagateChange();
  }

  @Nullable
  @Override
  public TreeSnapshot loadSnapshot() throws IOException {
    return id != null ? objService.readTree(id) : null;
  }

  @Nullable
  protected TreeSnapshot takeSnapshot(boolean persist, boolean allowEmpty) throws IOException {
    if(!isInitialized())
      return null;
    SortedMap<String, GitFileEntry> entries = new TreeMap<>();
    for(Map.Entry<String, Node> child : children.entrySet()) {
      Node node = child.getValue();
      AnyObjectId id = node.getObjectId(persist);
      if(id != null)
        entries.put(child.getKey(), new GitFileEntry(id, node.getMode()));
    }
    TreeSnapshot ret = TreeSnapshot.capture(entries, allowEmpty);
    if(ret != null && persist)
      objService.write(ret);
    return ret;
  }

  @Nullable
  @Override
  public TreeSnapshot takeSnapshot(boolean persist) throws IOException {
    return takeSnapshot(persist, false);
  }

  @Nonnull
  @Override
  public Node clone(@Nonnull DirectoryNode parent) throws IOException {
    DirectoryNode ret = DirectoryNode.newDirectory(parent);
    if(isInitialized()) {
      for(Map.Entry<String, Node> child : children.entrySet()) {
        String name = child.getKey();
        Node node = child.getValue();
        ret.addChild(name, node.clone(ret), false);
      }
    } else {
      ret.reset(id);
      parent.getObjService().pullObject(id, objService);
    }
    return ret;
  }

  @Nonnull
  public ConcurrentMap<String, Node> getChildren() throws IOException {
    loadSnapshotIfNotInitilized();
    return children;
  }

  public boolean hasChild(@Nonnull String name) throws IOException {
    loadSnapshotIfNotInitilized();
    return children.containsKey(name);
  }

  @Nullable
  public Node getChild(@Nonnull String name) throws IOException {
    loadSnapshotIfNotInitilized();
    return children.get(name);
  }

  public boolean addChild(@Nonnull String name, @Nonnull Node child, boolean replace) throws IOException {
    loadSnapshotIfNotInitilized();
    if(!replace && children.containsKey(name))
      return false;
    children.put(name, child);
    id = null;
    propagateChange();
    return true;
  }

  public boolean removeChild(@Nonnull String name) throws IOException {
    loadSnapshotIfNotInitilized();
    Node removed = children.remove(name);
    if(removed != null) {
      id = null;
      propagateChange();
      return true;
    }
    return false;
  }

  @Nullable
  public synchronized TreeSnapshot loadSnapshotIfNotInitilized() throws IOException {
    if(!isInitialized()) {
      setupEmptyDirectory();
      TreeSnapshot snapshot = loadSnapshot();
      if(snapshot != null)
        for(Map.Entry<String, GitFileEntry> entry : snapshot.getChildren().entrySet())
          children.put(entry.getKey(), Node.fromEntry(entry.getValue(), this));
      return snapshot;
    }
    return null;
  }

  public boolean isInitialized() {
    return children != null;
  }

  private void setupEmptyDirectory() {
    children = new ConcurrentHashMap<>();
  }

}
