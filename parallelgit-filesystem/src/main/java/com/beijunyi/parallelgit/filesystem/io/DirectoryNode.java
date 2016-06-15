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
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import static com.beijunyi.parallelgit.utils.io.GitFileEntry.*;
import static java.util.Collections.*;
import static org.eclipse.jgit.lib.FileMode.TREE;

public class DirectoryNode extends Node<TreeSnapshot, Map<String, Node>> {

  protected DirectoryNode(ObjectId id, GfsObjectService objService) {
    super(id, TREE, objService);
  }

  protected DirectoryNode(GfsObjectService objService) {
    super(TREE, objService);
  }

  protected DirectoryNode(ObjectId id, DirectoryNode parent) {
    super(id, TREE, parent);
  }

  protected DirectoryNode(DirectoryNode parent) {
    super(TREE, parent);
  }

  @Nonnull
  public static DirectoryNode fromTree(ObjectId id, DirectoryNode parent) {
    return new DirectoryNode(id, parent);
  }


  @Nonnull
  public static DirectoryNode newDirectory(DirectoryNode parent) {
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
  public void updateOrigin(GitFileEntry entry) throws IOException {
    super.updateOrigin(entry);
    if(isInitialized()) {
      if(origin.isSubtree()) {
        snapshot = objService.readTree(entry.getId());
        Set<String> updatedChildren = updateChildrenOrigins();
        Collection<Node> notUpdatedNodes = findNotUpdatedChildren(updatedChildren);
        updateOriginsToTrivial(notUpdatedNodes);
      } else {
        updateOriginsToTrivial(data.values());
      }
    }
  }

  public void updateOrigin(ObjectId id) throws IOException {
    updateOrigin(newTreeEntry(id));
  }

  @Nonnull
  @Override
  protected Map<String, Node> loadData(TreeSnapshot snapshot) throws IOException {
    Map<String, Node> ret = getDefaultData();
    boolean updateOrigin = origin != null && origin.getId().equals(snapshot.getId());
    for(Map.Entry<String, GitFileEntry> child : snapshot.getData().entrySet()) {
      GitFileEntry entry = child.getValue();
      Node node = Node.fromEntry(entry, this);
      ret.put(child.getKey(), node);
      if(updateOrigin)
        node.updateOrigin(entry);
    }
    return ret;
  }

  @Override
  protected boolean isTrivial(Map<String, Node> data) throws IOException {
    boolean ret = true;
    for(Node child : data.values())
      if(!child.isTrivial()) {
        ret = false;
        break;
      }
    return ret;
  }

  @Nonnull
  protected TreeSnapshot captureData(Map<String, Node> data, boolean persist) throws IOException {
    SortedMap<String, GitFileEntry> entries = new TreeMap<>();
    for(Map.Entry<String, Node> child : data.entrySet()) {
      Node node = child.getValue();
      ObjectId id = node.getObjectId(persist);
      if(!isTrivial(id))
        entries.put(child.getKey(), newEntry(id, node.getMode()));
    }
    return TreeSnapshot.capture(entries);
  }

  @Nonnull
  @Override
  public Node clone(DirectoryNode parent) throws IOException {
    DirectoryNode ret;
    if(isInitialized()) {
      ret = DirectoryNode.newDirectory(parent);
      for(Map.Entry<String, Node> child : data.entrySet()) {
        String name = child.getKey();
        Node node = child.getValue();
        ret.addChild(name, node.clone(ret), false);
      }
    } else if(id != null) {
      ret = DirectoryNode.fromTree(id, parent);
      parent.getObjectService().pullObject(id, objService);
    } else
      throw new IllegalStateException();
    return ret;
  }

  @Nonnull
  public List<String> listChildren() throws IOException {
    List<String> ret = new ArrayList<>(getData().keySet());
    Collections.sort(ret);
    return unmodifiableList(ret);
  }

  public boolean hasChild(String name) throws IOException {
    return getData().containsKey(name);
  }

  @Nullable
  public Node getChild(String name) throws IOException {
    return getData().get(name);
  }

  public boolean addChild(String name, Node child, boolean replace) throws IOException {
    if(!replace && getData().containsKey(name))
      return false;
    if(snapshot != null) {
      GitFileEntry origin = snapshot.getChild(name);
      if(!origin.isMissing()) child.updateOrigin(origin);
    }
    getData().put(name, child);
    id = null;
    invalidateParentCache();
    return true;
  }

  public boolean removeChild(String name) throws IOException {
    Node removed = getData().remove(name);
    if(removed != null) {
      removed.exile();
      id = null;
      invalidateParentCache();
      return true;
    }
    return false;
  }

  @Override
  protected void checkFileMode(FileMode proposed) {
    if(!TREE.equals(proposed))
      throw new IncompatibleFileModeException(TREE, proposed);
  }

  @Nonnull
  @Override
  protected Map<String, Node> getDefaultData() {
    return new ConcurrentHashMap<>();
  }

  @Nonnull
  private Set<String> updateChildrenOrigins() throws IOException {
    Set<String> ret = new HashSet<>();
    for(Map.Entry<String, GitFileEntry> child : snapshot.getData().entrySet()) {
      String name = child.getKey();
      Node node = data.get(name);
      if(node != null && !node.getOrigin().equals(child.getValue()))
        node.updateOrigin(child.getValue());
      ret.add(name);
    }
    return unmodifiableSet(ret);
  }

  @Nonnull
  private Collection<Node> findNotUpdatedChildren(Set<String> updatedChildren) throws IOException{
    List<Node> ret = new ArrayList<>();
    for(Map.Entry<String, Node> child : data.entrySet()) {
      String name = child.getKey();
      if(!updatedChildren.contains(name))
        ret.add(child.getValue());
    }
    return unmodifiableList(ret);
  }

  private void updateOriginsToTrivial(Collection<Node> nodes) throws IOException {
    for(Node node : nodes) {
      node.updateOrigin(missingEntry());
    }
  }

  @Override
  protected void exile() {
    super.exile();
    if(isInitialized()) {
      for(Node child : data.values())
        child.exile();
    }
  }

}
