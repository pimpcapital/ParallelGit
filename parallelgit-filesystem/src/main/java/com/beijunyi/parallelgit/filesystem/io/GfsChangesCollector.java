package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.util.*;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.utils.io.GitFileEntry;
import org.eclipse.jgit.lib.FileMode;

import static com.beijunyi.parallelgit.filesystem.utils.GfsPathUtils.*;

public class GfsChangesCollector {

  private static final GfsChange DELETE_NODE = new DeleteNode();
  private static final GfsChange PREPARE_DIRECTORY = new MakeDirectory();

  private final Map<String, GfsChange> changes = new HashMap<>();
  private final Map<String, Set<String>> changedDirs = new HashMap<>();

  private final Queue<DirectoryNode> dirs = new LinkedList<>();
  private final Queue<String> paths = new LinkedList<>();

  public boolean isEmpty() {
    return changes.isEmpty();
  }

  public void addChange(String path, GfsChange change) {
    if(changes.containsKey(path))
      throw new IllegalStateException();
    changes.put(path, change);
    addChangedDirectory(path);
  }

  public void addChange(String path, GitFileEntry entry) {
    GfsChange change;
    if(entry.isMissing())
      change = DELETE_NODE;
    else if(entry.isVirtualSubtree())
      change = PREPARE_DIRECTORY;
    else
      change = new UpdateNode(entry);
    addChange(path, change);
  }

  public void addChange(String path, byte[] bytes, FileMode mode) {
    addChange(path, new UpdateFile(bytes, mode));
  }

  public void applyTo(GitFileSystem gfs) throws IOException {
    dirs.add(gfs.getFileStore().getRoot());
    paths.add("/");
    while(!dirs.isEmpty()) {
      DirectoryNode dir = dirs.poll();
      String path = paths.poll();
      applyChangesToDir(dir, path);
    }
  }

  private void addChangedDirectory(String path) {
    if(isRoot(path))
      return;
    int parentLength = path.lastIndexOf('/');
    if(parentLength >= 0) {
      String parent = path.substring(0, parentLength == 0 ? 1 : parentLength);
      String filename = path.substring(parentLength + 1);
      Set<String> siblings = childrenOf(parent);
      if(siblings.add(filename))
        addChangedDirectory(parent);
    }
  }

  @Nonnull
  private Set<String> childrenOf(String parent) {
    Set<String> ret = changedDirs.get(parent);
    if(ret == null) {
      ret = new HashSet<>();
      changedDirs.put(parent, ret);
    }
    return ret;
  }

  private void applyChangesToDir(DirectoryNode dir, String path) throws IOException {
    String prefix = addTrailingSlash(path);
    for(String childName : childrenOf(path)) {
      String childPath = prefix + childName;
      GfsChange change = changes.get(childPath);
      if(change != null)
        change.applyTo(dir, childName);
      if(changedDirs.containsKey(childPath))
        addSubDirectoryToQueue(childPath, childName, dir);
    }
  }

  private void addSubDirectoryToQueue(String childPath, String childName, DirectoryNode dir) throws IOException {
    DirectoryNode child = (DirectoryNode) dir.getChild(childName);
    if(child == null) {
      child = DirectoryNode.newDirectory(dir);
      dir.addChild(childName, child, false);
    }
    dirs.add(child);
    paths.add(childPath);
  }

}
