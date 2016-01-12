package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.util.*;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.utils.io.GitFileEntry;

public class GfsChangeCollector {

  private final Map<String, GitFileEntry> entries = new HashMap<>();
  private final Map<String, Set<String>> changedDirs = new HashMap<>();

  private final Queue<DirectoryNode> dirs = new LinkedList<>();
  private final Queue<String> paths = new LinkedList<>();

  public void addChange(@Nonnull String path, @Nonnull GitFileEntry entry) {
    path = toAbsolutePath(path);
    entries.put(path, entry);
    addChangedDirectory(path);
  }

  public void applyTo(@Nonnull GitFileSystem gfs) throws IOException {
    dirs.add(gfs.getFileStore().getRoot());
    paths.add("/");
    while(!dirs.isEmpty()) {
      DirectoryNode dir = dirs.poll();
      String path = paths.poll();
      applyChangesToDir(dir, path);
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
    String prefix = ensureTrailingSlash(path);
    for(String childName : changedDirs.get(path)) {
      String childPath = prefix + childName;
      GitFileEntry change = entries.get(childPath);
      if(change == null)
        addSubDirectoryToQueue(childPath, childName, dir);
      else if(change.isMissing())
        dir.removeChild(childName);
      else
        dir.addChild(childName, Node.fromEntry(change, dir.getObjService()), true);
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

  @Nonnull
  private static String toAbsolutePath(@Nonnull String path) {
    return path.startsWith("/") ? path : ("/" + path);
  }

  @Nonnull
  private static String ensureTrailingSlash(@Nonnull String path) {
    return path.endsWith("/") ? path : (path + "/");
  }

  private static boolean isRoot(@Nonnull String path) {
    return "/".equals(path);
  }

}
