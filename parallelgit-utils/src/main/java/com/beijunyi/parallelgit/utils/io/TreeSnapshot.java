package com.beijunyi.parallelgit.utils.io;

import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.TreeUtils;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.treewalk.TreeWalk;

import static com.beijunyi.parallelgit.utils.io.GitFileEntry.*;
import static java.util.Collections.unmodifiableSortedMap;
import static org.eclipse.jgit.lib.Constants.OBJ_TREE;

public class TreeSnapshot extends ObjectSnapshot<SortedMap<String, GitFileEntry>> {

  private TreeSnapshot(ObjectId id, SortedMap<String, GitFileEntry> data) {
    super(id, unmodifiableSortedMap(data));
  }

  public boolean hasChild(String name) {
    return data.containsKey(name);
  }

  @Nonnull
  public GitFileEntry getChild(String name) {
    GitFileEntry entry = data.get(name);
    return entry != null ? entry : missingEntry();
  }

  @Nonnull
  public ObjectId save(ObjectInserter inserter) throws IOException {
    return inserter.insert(format(data));
  }

  @Nonnull
  public ObjectId save(Repository repo) throws IOException {
    try(ObjectInserter inserter = repo.newObjectInserter()) {
      ObjectId ret = save(inserter);
      inserter.flush();
      return ret;
    }
  }

  @Nonnull
  public static TreeSnapshot load(ObjectId id, ObjectReader reader) throws IOException {
    SortedMap<String, GitFileEntry> ret = new TreeMap<>();
    try(TreeWalk tw = TreeUtils.newTreeWalk(id, reader)) {
      while(tw.next()) ret.put(tw.getNameString(), newEntry(tw));
    }
    return new TreeSnapshot(id, ret);
  }

  @Nonnull
  public static TreeSnapshot load(ObjectId id, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return load(id, reader);
    }
  }

  @Nonnull
  public static TreeSnapshot capture(SortedMap<String, GitFileEntry> children) {
    return new TreeSnapshot(computeTreeId(children), children);
  }

  @Nonnull
  private static TreeFormatter format(SortedMap<String, GitFileEntry> data) {
    TreeFormatter formatter = new TreeFormatter();
    for(Map.Entry<String, GitFileEntry> child : data.entrySet()) {
      String name = child.getKey();
      GitFileEntry entry = child.getValue();
      formatter.append(name, entry.getMode(), entry.getId());
    }
    return formatter;
  }

  @Nonnull
  private static ObjectId computeTreeId(SortedMap<String, GitFileEntry> data) {
    return new ObjectInserter.Formatter().idFor(OBJ_TREE, format(data).toByteArray());
  }

}
