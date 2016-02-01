package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsObjectService;
import com.beijunyi.parallelgit.filesystem.exceptions.IncompatibleFileModeException;
import com.beijunyi.parallelgit.utils.io.GitFileEntry;
import com.beijunyi.parallelgit.utils.io.TreeSnapshot;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

import static org.eclipse.jgit.lib.FileMode.TREE;

public class DirectoryNode extends Node<TreeSnapshot, Map<String, Node>> {

  protected DirectoryNode(@Nonnull GitFileEntry entry, @Nonnull GfsObjectService objService) {
    super(entry, objService);
  }

  protected DirectoryNode(@Nonnull AnyObjectId id, @Nonnull GfsObjectService objService) {
    this(new GitFileEntry(id, TREE), objService);
  }

  protected DirectoryNode(@Nonnull GfsObjectService objService) {
    super(TREE, objService);
  }

  protected DirectoryNode(@Nonnull GitFileEntry entry, @Nonnull DirectoryNode parent) {
    super(entry, parent);
  }

  protected DirectoryNode(@Nonnull AnyObjectId id, @Nonnull DirectoryNode parent) {
    this(GitFileEntry.tree(id), parent);
  }

  protected DirectoryNode(@Nonnull DirectoryNode parent) {
    super(TREE, parent);
  }

  @Nonnull
  public static DirectoryNode fromFileEntry(@Nonnull GitFileEntry entry, @Nonnull DirectoryNode parent) {
    return new DirectoryNode(entry, parent);
  }

  @Nonnull
  public static DirectoryNode newDirectory(@Nonnull DirectoryNode parent) {
    return new DirectoryNode(parent);
  }

  @Override
  protected Class<? extends TreeSnapshot> getSnapshotType() {
    return TreeSnapshot.class;
  }

  @Override
  public long getSize() throws IOException {
    return 0;
  }

  @Override
  public synchronized void updateOrigin(@Nonnull GitFileEntry entry) throws IOException {
    super.updateOrigin(entry);
    if(isInitialized()) {
      snapshot = objService.readTree(id);
      for(Map.Entry<String, GitFileEntry> child : snapshot.getChildren().entrySet()) {
        String name = child.getKey();
        Node node = data.get(name);
        if(node != null)
          node.updateOrigin(child.getValue());
      }
    }
  }

  public void updateOrigin(@Nonnull AnyObjectId id) throws IOException {
    updateOrigin(GitFileEntry.tree(id));
  }

  @Nonnull
  @Override
  protected Map<String, Node> loadData(@Nonnull TreeSnapshot snapshot) {
    Map<String, Node> ret = getDefaultData();
    for(Map.Entry<String, GitFileEntry> entry : snapshot.getChildren().entrySet())
      data.put(entry.getKey(), Node.fromEntry(entry.getValue(), this));
    return ret;
  }

  @Override
  protected boolean isTrivial(@Nonnull Map<String, Node> data) {
    return data.isEmpty();
  }

  @Nonnull
  protected TreeSnapshot captureData(@Nonnull Map<String, Node> data, boolean persist) throws IOException {
    SortedMap<String, GitFileEntry> entries = new TreeMap<>();
    for(Map.Entry<String, Node> child : data.entrySet()) {
      Node node = child.getValue();
      AnyObjectId id = node.getObjectId(persist);
      if(id != null)
        entries.put(child.getKey(), new GitFileEntry(id, node.getMode()));
    }
    return TreeSnapshot.capture(entries);
  }

  @Nonnull
  @Override
  public Node clone(@Nonnull DirectoryNode parent) throws IOException {
    DirectoryNode ret = DirectoryNode.newDirectory(parent);
    if(isInitialized()) {
      for(Map.Entry<String, Node> child : data.entrySet()) {
        String name = child.getKey();
        Node node = child.getValue();
        ret.addChild(name, node.clone(ret), false);
      }
    } else {
      ret.reset(origin);
      parent.getObjectService().pullObject(origin.getId(), objService);
    }
    return ret;
  }

  @Nonnull
  public List<String> listChildren() throws IOException {
    List<String> ret;
    synchronized(this) {
      prepareChildren();
      ret = new ArrayList<>(data.keySet());
    }
    Collections.sort(ret);
    return Collections.unmodifiableList(ret);
  }

  public boolean hasChild(@Nonnull String name) throws IOException {
    synchronized(this) {
      prepareChildren();
      return data.containsKey(name);
    }
  }

  @Nullable
  public Node getChild(@Nonnull String name) throws IOException {
    synchronized(this) {
      prepareChildren();
      return data.get(name);
    }
  }

  public boolean addChild(@Nonnull String name, @Nonnull Node child, boolean replace) throws IOException {
    synchronized(this) {
      prepareChildren();
      if(!replace && data.containsKey(name))
        return false;
      data.put(name, child);
    }
    id = null;
    invalidateParentCache();
    return true;
  }

  public boolean removeChild(@Nonnull String name) throws IOException {
    Node removed;
    synchronized(this) {
      prepareChildren();
      removed = data.remove(name);
    }
    if(removed != null) {
      removed.exile();
      id = null;
      invalidateParentCache();
      return true;
    }
    return false;
  }

  private void prepareChildren() throws IOException {
    if(!isInitialized()) {
      initialize();
      snapshot = objService.readTree(origin.getId());
      for(Map.Entry<String, GitFileEntry> entry : snapshot.getChildren().entrySet())
        data.put(entry.getKey(), Node.fromEntry(entry.getValue(), this));
    }
  }

  @Override
  protected void checkFileMode(@Nonnull FileMode proposed) {
    if(!TREE.equals(proposed))
      throw new IncompatibleFileModeException(TREE, proposed);
  }

  @Nonnull
  @Override
  protected Map<String, Node> getDefaultData() {
    return new ConcurrentHashMap<>();
  }

  @Override
  protected void exile() {
    super.exile();
    synchronized(this) {
      if(isInitialized()) {
        for(Node child : data.values())
          child.exile();
      }
    }
  }

}
