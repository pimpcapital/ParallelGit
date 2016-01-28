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
import com.beijunyi.parallelgit.utils.io.ObjectSnapshot;
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

  @Nonnull
  public static DirectoryNode fromObject(@Nonnull AnyObjectId id, @Nonnull GfsObjectService objService) {
    return new DirectoryNode(id, objService);
  }

  @Nonnull
  public static DirectoryNode newDirectory(@Nonnull GfsObjectService objService) {
    return new DirectoryNode(null, objService);
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
      ObjectSnapshot snapshot = node.takeSnapshot(persist);
      AnyObjectId id = snapshot != null ? snapshot.getId() : node.getObjectId();
      if(id != null)
        entries.put(child.getKey(), new GitFileEntry(id, node.getMode()));
    }
    TreeSnapshot ret = TreeSnapshot.capture(entries, allowEmpty);
    if(persist)
      id = ret != null ? objService.write(ret) : null;
    return ret;
  }

  @Nullable
  @Override
  public TreeSnapshot takeSnapshot(boolean persist) throws IOException {
    return takeSnapshot(persist, false);
  }

  @Nonnull
  @Override
  public Node clone(@Nonnull GfsObjectService targetObjService) throws IOException {
    DirectoryNode ret = DirectoryNode.newDirectory(targetObjService);
    if(isInitialized()) {
      for(Map.Entry<String, Node> child : children.entrySet()) {
        String name = child.getKey();
        Node node = child.getValue();
        ret.addChild(name, node.clone(targetObjService), false);
      }
    } else {
      ret.reset(id);
      targetObjService.pullObject(id, objService);
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
    return true;
  }

  public boolean removeChild(@Nonnull String name) throws IOException {
    loadSnapshotIfNotInitilized();
    Node removed = children.remove(name);
    return removed != null;
  }

  @Nullable
  public synchronized TreeSnapshot loadSnapshotIfNotInitilized() throws IOException {
    if(!isInitialized()) {
      setupEmptyDirectory();
      TreeSnapshot snapshot = loadSnapshot();
      if(snapshot != null)
        for(Map.Entry<String, GitFileEntry> entry : snapshot.getChildren().entrySet())
          children.put(entry.getKey(), Node.fromEntry(entry.getValue(), objService));
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
