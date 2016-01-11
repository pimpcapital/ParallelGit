package com.beijunyi.parallelgit.filesystem.io;

import java.util.*;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.utils.io.GitFileEntry;

public class GfsChangeCollector {

  private final Map<String, GitFileEntry> entries = new HashMap<>();
  private final Map<String, Set<String>> changedPaths = new HashMap<>();

  public void addChange(@Nonnull String path, @Nonnull GitFileEntry entry) {
    entries.put(path, entry);
    addParents(path);
  }

  public void addParents(@Nonnull String path) {
    if(path.length() == 1)
      return;
    int parentEnd = path.lastIndexOf('/');
    if(parentEnd >= 0) {
      String parent = path.substring(0, parentEnd == 0 ? 1 : parentEnd);
      Set<String> siblings = changedPaths.get(parent);
      if(siblings == null) {
        siblings = new HashSet<>();
        changedPaths.put(parent, siblings);
      }
      siblings.add(path);
      addParents(parent);
    }
  }

  public void applyTo(@Nonnull GitFileSystem gfs) {
    Queue<DirectoryNode> dirs = new LinkedList<>();
    dirs.add(gfs.getFileStore().getRoot());
    Queue<String> paths = new LinkedList<>();
    paths.add("/");
    while(!dirs.isEmpty()) {
      DirectoryNode dir = dirs.poll();
      String path = paths.poll();
      Set<String> children = changedPaths.get(path);
    }
  }

}
