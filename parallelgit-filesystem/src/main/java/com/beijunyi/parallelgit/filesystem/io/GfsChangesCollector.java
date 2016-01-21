package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.util.*;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.utils.io.GitFileEntry;

import static com.beijunyi.parallelgit.filesystem.utils.GfsPathUtils.*;

public class GfsChangesCollector {

  private static final GfsChange DELETE_NODE = new DeleteNode();
  private static final GfsChange PREPARE_DIRECTORY = new PrepareDirectory();

  private final Map<String, GfsChange> changes = new HashMap<>();
  private final Map<String, Set<String>> changedDirs = new HashMap<>();

  private final Queue<DirectoryNode> dirs = new LinkedList<>();
  private final Queue<String> paths = new LinkedList<>();

  public void addChange(@Nonnull String path, @Nonnull GfsChange change) {
    if(changes.containsKey(path))
      throw new IllegalStateException();
    changes.put(path, change);
    addChangedDirectory(path);
  }

  public void addChange(@Nonnull String path, @Nonnull GitFileEntry entry) {
    GfsChange change;
    if(entry.isMissing())
      change = DELETE_NODE;
    else if(entry.isVirtualDirectory())
      change = PREPARE_DIRECTORY;
    else
      change = new UpdateNode(entry);
    addChange(path, change);
  }

  public void applyTo(@Nonnull GitFileSystem gfs) throws IOException {
    if(!changes.isEmpty()) {
      dirs.add(gfs.getFileStore().getRoot());
      paths.add("/");
      while(!dirs.isEmpty()) {
        DirectoryNode dir = dirs.poll();
        String path = paths.poll();
        applyChangesToDir(dir, path);
      }
    }
  }

  private void addChangedDirectory(@Nonnull String path) {
    if(isRoot(path))
      return;
    int parentLength = path.lastIndexOf('/');
    if(parentLength >= 0) {
      String parent = path.substring(0, parentLength == 0 ? 1 : parentLength);
      String filename = path.substring(parentLength + 1);
      Set<String> siblings = getChildrenOf(parent);
      if(siblings.add(filename))
        addChangedDirectory(parent);
    }
  }

  @Nonnull
  private Set<String> getChildrenOf(@Nonnull String parent) {
    Set<String> ret = changedDirs.get(parent);
    if(ret == null) {
      ret = new HashSet<>();
      changedDirs.put(parent, ret);
    }
    return ret;
  }

  private void applyChangesToDir(@Nonnull DirectoryNode dir, @Nonnull String path) throws IOException {
    String prefix = addTrailingSlash(path);
    for(String childName : changedDirs.get(path)) {
      String childPath = prefix + childName;
      GfsChange change = changes.get(childPath);
      if(change != null)
        change.applyTo(dir, childName);
      if(changedDirs.containsKey(childPath))
        addSubDirectoryToQueue(childPath, childName, dir);
    }
  }

  private void addSubDirectoryToQueue(@Nonnull String childPath, @Nonnull String childName, @Nonnull DirectoryNode dir) throws IOException {
    DirectoryNode child = (DirectoryNode) dir.getChild(childName);
    if(child == null) {
      child = DirectoryNode.newDirectory(dir.getObjService());
      dir.addChild(childName, child, false);
    }
    dirs.add(child);
    paths.add(childPath);
  }

}
