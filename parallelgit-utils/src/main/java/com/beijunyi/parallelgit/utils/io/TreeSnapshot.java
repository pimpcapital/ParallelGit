package com.beijunyi.parallelgit.utils.io;

import java.io.IOException;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.TreeUtils;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.treewalk.TreeWalk;

public class TreeSnapshot extends ObjectSnapshot {

  private final Map<String, GitFileEntry> children;

  private TreeSnapshot(@Nonnull Map<String, GitFileEntry> children) {
    this.children = Collections.unmodifiableMap(new TreeMap<>(children));
  }

  @Nonnull
  public Map<String, GitFileEntry> getChildren() {
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
    HashMap<String, GitFileEntry> ret = new HashMap<>();
    try(TreeWalk tw = TreeUtils.newTreeWalk(id, reader)) {
      while(tw.next())
        ret.put(tw.getNameString(), new GitFileEntry(tw.getObjectId(0), tw.getFileMode(0)));
    }
    return new TreeSnapshot(ret);
  }

  @Nullable
  public static TreeSnapshot capture(@Nonnull Map<String, GitFileEntry> children, boolean allowEmpty) {
    return allowEmpty || !children.isEmpty() ? new TreeSnapshot(children) : null;
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
