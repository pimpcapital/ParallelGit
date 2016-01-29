package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.util.*;
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
    super(id, FileMode.TREE, objService);
    if(id == null)
      setupEmptyDirectory();
  }

  protected DirectoryNode(@Nullable AnyObjectId id, @Nonnull DirectoryNode parent) {
    super(id, FileMode.TREE, parent);
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
    checkFileMode(mode);
  }

  @Override
  public synchronized void reset(@Nonnull AnyObjectId id, @Nonnull FileMode mode) {
    checkFileMode(mode);
    this.id = id;
    children = null;
    propagateChange();
  }

  @Override
  public synchronized void updateOrigin(@Nonnull AnyObjectId id, @Nonnull FileMode mode) throws IOException {
    checkFileMode(mode);
    if(children != null) {
      TreeSnapshot snapshot = objService.readTree(id);
      for(Map.Entry<String, GitFileEntry> child : snapshot.getChildren().entrySet()) {
        String name = child.getKey();
        Node node = children.get(name);
        if(node != null) {
          GitFileEntry entry = child.getValue();
          node.updateOrigin(entry.getId(), entry.getMode());
        }
      }
    }
    originId = getObjectId(true);
  }

  public void updateOrigin(@Nonnull AnyObjectId id) throws IOException {
    updateOrigin(id, FileMode.TREE);
  }

  @Nullable
  @Override
  public TreeSnapshot getSnapshot(boolean persist) throws IOException {
    TreeSnapshot ret = takeSnapshot(persist);
    if(ret == null && id != null)
      ret = objService.readTree(id);
    return ret;
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
      ret.reset(id, mode);
      parent.getObjService().pullObject(id, objService);
    }
    return ret;
  }

  @Nonnull
  public List<String> listChildren() throws IOException {
    List<String> ret;
    synchronized(this) {
      prepareChildren();
      ret = new ArrayList<>(children.keySet());
    }
    Collections.sort(ret);
    return Collections.unmodifiableList(ret);
  }

  public boolean hasChild(@Nonnull String name) throws IOException {
    synchronized(this) {
      prepareChildren();
      return children.containsKey(name);
    }
  }

  @Nullable
  public Node getChild(@Nonnull String name) throws IOException {
    synchronized(this) {
      prepareChildren();
      return children.get(name);
    }
  }

  public boolean addChild(@Nonnull String name, @Nonnull Node child, boolean replace) throws IOException {
    synchronized(this) {
      prepareChildren();
      if(!replace && children.containsKey(name))
        return false;
      children.put(name, child);
    }
    id = null;
    propagateChange();
    return true;
  }

  public boolean removeChild(@Nonnull String name) throws IOException {
    Node removed;
    synchronized(this) {
      prepareChildren();
      removed = children.remove(name);
    }
    if(removed != null) {
      removed.disconnectParent();
      id = null;
      propagateChange();
      return true;
    }
    return false;
  }

  public void prepareChildren() throws IOException {
    if(!isInitialized()) {
      setupEmptyDirectory();
      TreeSnapshot snapshot = id != null ? objService.readTree(id) : null;
      if(snapshot != null)
        for(Map.Entry<String, GitFileEntry> entry : snapshot.getChildren().entrySet())
          children.put(entry.getKey(), Node.fromEntry(entry.getValue(), this));
    }
  }

  public boolean isInitialized() {
    return children != null;
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
  protected TreeSnapshot takeSnapshot(boolean persist) throws IOException {
    return takeSnapshot(persist, false);
  }

  @Override
  protected void disconnectParent() {
    super.disconnectParent();
    synchronized(this) {
      if(isInitialized()) {
        for(Node child : children.values())
          child.disconnectParent();
      }
    }
  }

  private void setupEmptyDirectory() {
    children = new ConcurrentHashMap<>();
  }

  private static void checkFileMode(@Nonnull FileMode mode) {
    if(!mode.equals(TREE))
      throw new IllegalArgumentException(mode.toString());
  }

}
