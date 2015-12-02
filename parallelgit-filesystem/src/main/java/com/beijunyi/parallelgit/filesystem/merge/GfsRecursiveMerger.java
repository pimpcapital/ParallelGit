package com.beijunyi.parallelgit.filesystem.merge;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.io.GitFileEntry;

public class GfsRecursiveMerger {

  private final ThreeWayWalker walker;
  private final ContentMergeConfig config;
  private final Map<String, GfsMergeConflict> conflicts = new HashMap<>();

  public GfsRecursiveMerger(@Nonnull ThreeWayWalker walker, @Nonnull ContentMergeConfig config) {
    this.walker = walker;
    this.config = config;
  }

  @Nonnull
  public Map<String, GfsMergeConflict> merge() throws IOException {
    while(walker.hasNext()) {
      ThreeWayEntry entry = walker.next();
      mergeEntry(entry);
    }
    IOException error = walker.getError();
    if(error != null)
      throw error;
    return conflicts;
  }

  private boolean mergeEntry(@Nonnull ThreeWayEntry entry) {
    return objectMerge(entry) || contentMerge(entry);
  }

  private boolean objectMerge(@Nonnull ThreeWayEntry entry) {
    if(entry.getBase().equals(entry.getOurs())) {
      apply(entry.getTheirs());
      return true;
    }
    if(entry.getBase().equals(entry.getTheirs())) {
      apply(entry.getOurs());
      return true;
    }
    return false;
  }

  private void apply(@Nonnull GitFileEntry entry) {

  }

  private boolean contentMerge(@Nonnull ThreeWayEntry entry) {
    return true;
  }

}
