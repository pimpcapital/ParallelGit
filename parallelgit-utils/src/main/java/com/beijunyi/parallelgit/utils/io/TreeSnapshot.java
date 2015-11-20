package com.beijunyi.parallelgit.utils.io;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

import org.eclipse.jgit.treewalk.TreeWalk;

public class TreeSnapshot {

  private final Map<String, GitFileEntry> children;

  public TreeSnapshot(@Nonnull TreeWalk tw) throws IOException {
    this(wrap(tw));
  }

  private TreeSnapshot(@Nonnull Map<String, GitFileEntry> children) {
    this.children = children;
  }

  @Nonnull
  public Map<String, GitFileEntry> getChildren() {
    return children;
  }

  @Nonnull
  private static Map<String, GitFileEntry> wrap(@Nonnull TreeWalk tw) throws IOException {
    HashMap<String, GitFileEntry> ret = new HashMap<>();
    while(tw.next())
      ret.put(tw.getNameString(), new GitFileEntry(tw.getObjectId(0), tw.getFileMode(0)));
    return Collections.unmodifiableMap(ret);
  }

}
