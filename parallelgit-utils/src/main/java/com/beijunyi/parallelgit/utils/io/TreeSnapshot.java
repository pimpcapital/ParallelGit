package com.beijunyi.parallelgit.utils.io;

import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.TreeUtils;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.treewalk.TreeWalk;

public class TreeSnapshot extends ObjectSnapshot<SortedMap<String, GitFileEntry>> {

  private TreeSnapshot(@Nonnull AnyObjectId id, @Nonnull SortedMap<String, GitFileEntry> data) {
    super(id, data);
  }

  @Nullable
  public GitFileEntry getChild(@Nonnull String name) {
    return data.get(name);
  }

  @Nonnull
  public AnyObjectId persist(@Nonnull ObjectInserter inserter) throws IOException {
    return inserter.insert(format(data));
  }

  @Nonnull
  public static TreeSnapshot load(@Nonnull AnyObjectId id, @Nonnull ObjectReader reader) throws IOException {
    SortedMap<String, GitFileEntry> ret = new TreeMap<>();
    try(TreeWalk tw = TreeUtils.newTreeWalk(id, reader)) {
      while(tw.next())
        ret.put(tw.getNameString(), new GitFileEntry(tw.getObjectId(0), tw.getFileMode(0)));
    }
    return new TreeSnapshot(id, ret);
  }

  @Nonnull
  public static TreeSnapshot capture(@Nonnull SortedMap<String, GitFileEntry> children) {
    return new TreeSnapshot(computeTreeId(children), children);
  }

  @Nonnull
  private static TreeFormatter format(@Nonnull SortedMap<String, GitFileEntry> data) {
    TreeFormatter formatter = new TreeFormatter();
    for(Map.Entry<String, GitFileEntry> child : data.entrySet()) {
      String name = child.getKey();
      GitFileEntry entry = child.getValue();
      formatter.append(name, entry.getMode(), entry.getId());
    }
    return formatter;
  }

  @Nonnull
  private static AnyObjectId computeTreeId(@Nonnull SortedMap<String, GitFileEntry> data) {
    return new ObjectInserter.Formatter().idFor(Constants.OBJ_TREE, format(data).toByteArray());
  }

}
