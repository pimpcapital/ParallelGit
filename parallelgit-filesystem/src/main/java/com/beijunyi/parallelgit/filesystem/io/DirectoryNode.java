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
  }

  @Nonnull
  public static DirectoryNode fromObject(@Nonnull AnyObjectId id, @Nonnull GfsObjectService objService) {
    return new DirectoryNode(id, objService);
  }

  @Nonnull
  public static DirectoryNode newDirectory(@Nonnull GfsObjectService objService) {
    DirectoryNode ret = new DirectoryNode(null, objService);
    ret.setupEmptyDirectory();
    return ret;
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
  @Override
  public TreeSnapshot takeSnapshot(boolean persist, boolean allowEmpty) throws IOException {
    if(!isInitialized())
      return null;
    SortedMap<String, GitFileEntry> entries = new TreeMap<>();
    for(Map.Entry<String, Node> child : children.entrySet()) {
      Node node = child.getValue();
      ObjectSnapshot snapshot = node.takeSnapshot(persist, false);
      AnyObjectId id = snapshot != null ? snapshot.getId() : node.getObjectId();
      if(id != null)
        entries.put(child.getKey(), new GitFileEntry(id, node.getMode()));
    }
    TreeSnapshot ret = TreeSnapshot.capture(entries, allowEmpty);
    if(persist)
      id = ret != null ? objService.write(ret) : null;
    return ret;
  }

  @Nonnull
  @Override
  public Node clone(@Nonnull GfsObjectService targetObjService) throws IOException {
    DirectoryNode ret = new DirectoryNode(null, targetObjService);
    if(isInitialized()) {
      for(Map.Entry<String, Node> child : children.entrySet()) {
        String name = child.getKey();
        Node node = child.getValue();
        ret.addChild(name, node.clone(targetObjService), false);
      }
    } else {
      ret.id = id;
      targetObjService.pullObject(id, objService);
    }
    return ret;
  }

  @Nonnull
  public ConcurrentMap<String, Node> getChildren() throws IOException {
    loadChildren();
    return children;
  }

  public boolean hasChild(@Nonnull String name) throws IOException {
    loadChildren();
    return children.containsKey(name);
  }

  @Nullable
  public Node getChild(@Nonnull String name) throws IOException {
    loadChildren();
    return children.get(name);
  }

  public boolean addChild(@Nonnull String name, @Nonnull Node child, boolean replace) throws IOException {
    loadChildren();
    if(!replace && children.containsKey(name))
      return false;
    children.put(name, child);
    return true;
  }

  public boolean removeChild(@Nonnull String name) throws IOException {
    loadChildren();
    Node removed = children.remove(name);
    return removed != null;
  }

  private synchronized void loadChildren() throws IOException {
    if(!isInitialized()) {
      setupEmptyDirectory();
      TreeSnapshot tree = loadSnapshot();
      if(tree != null)
        for(Map.Entry<String, GitFileEntry> entry : tree.getChildren().entrySet())
          children.put(entry.getKey(), Node.fromEntry(entry.getValue(), objService));
    }
  }

  private boolean isInitialized() {
    return children != null;
  }

  private void setupEmptyDirectory() {
    children = new ConcurrentHashMap<>();
  }

}
