package com.beijunyi.parallelgit.filesystem.merge;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.io.GitFileEntry;

public class GfsIterativeMerger {

  private final ThreeWayWalker walker;
  private final ContentMergeConfig config;

  public GfsIterativeMerger(@Nonnull ThreeWayWalker walker, @Nonnull ContentMergeConfig config) {
    this.walker = walker;
    this.config = config;
  }

  public void merge() throws IOException {
    while(walker.hasNext()) {
      ThreeWayEntry entry = walker.next();
      mergeEntry(entry);
    }
    if(walker.getError() != null)
      throw walker.getError();
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
