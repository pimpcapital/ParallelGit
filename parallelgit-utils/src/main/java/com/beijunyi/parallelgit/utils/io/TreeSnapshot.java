package com.beijunyi.parallelgit.utils.io;

import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.TreeUtils;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.treewalk.TreeWalk;

import static java.util.Collections.unmodifiableSortedMap;

public class TreeSnapshot extends ObjectSnapshot {

  private final SortedMap<String, GitFileEntry> children;

  private TreeSnapshot(@Nonnull SortedMap<String, GitFileEntry> children) {
    this.children = unmodifiableSortedMap(children);
  }

  @Nonnull
  public SortedMap<String, GitFileEntry> getChildren() {
    return children;
  }

  @Nonnull
  public AnyObjectId computeId() {
    return new ObjectInserter.Formatter().idFor(Constants.OBJ_TREE, format().toByteArray());
  }

  @Nonnull
  public AnyObjectId persist(@Nonnull ObjectInserter inserter) throws IOException {
    return inserter.insert(format());
  }

  @Nonnull
  public static TreeSnapshot load(@Nonnull AnyObjectId id, @Nonnull ObjectReader reader) throws IOException {
    SortedMap<String, GitFileEntry> ret = new TreeMap<>();
    try(TreeWalk tw = TreeUtils.newTreeWalk(id, reader)) {
      while(tw.next())
        ret.put(tw.getNameString(), new GitFileEntry(tw.getObjectId(0), tw.getFileMode(0)));
    }
    return new TreeSnapshot(ret);
  }

  @Nonnull
  public static TreeSnapshot capture(@Nonnull SortedMap<String, GitFileEntry> children) {
    return new TreeSnapshot(children);
  }

  @Nonnull
  private TreeFormatter format() {
    TreeFormatter formatter = new TreeFormatter();
    for(Map.Entry<String, GitFileEntry> child : children.entrySet()) {
      String name = child.getKey();
      GitFileEntry entry = child.getValue();
      formatter.append(name, entry.getMode(), entry.getId());
    }
    return formatter;
  }

}
