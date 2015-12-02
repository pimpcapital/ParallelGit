package com.beijunyi.parallelgit.filesystem.merge;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.io.GitFileEntry;
import org.eclipse.jgit.lib.FileMode;

import static org.eclipse.jgit.lib.FileMode.MISSING;

public class GfsRecursiveMerger {

  private final ThreeWayWalker walker;
  private final ContentMergeConfig config;
  private final Map<String, GfsMergeConflict> conflicts = new HashMap<>();

  public GfsRecursiveMerger(@Nonnull ThreeWayWalker walker, @Nullable ContentMergeConfig config) {
    this.walker = walker;
    this.config = config;
  }

  @Nonnull
  public Map<String, GfsMergeConflict> getConflicts() {
    return conflicts;
  }

  public boolean merge() throws IOException {
    while(walker.hasNext()) {
      ThreeWayEntry entry = walker.next();
      mergeEntry(entry);
    }
    IOException error = walker.getError();
    if(error != null)
      throw error;
    return conflicts.isEmpty();
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
    if(entry.getOurs().getId().equals(entry.getTheirs().getId())) {
      if(entry.getOurs().getMode().equals(entry.getTheirs().getMode())) {
        apply(entry.getOurs());
        return true;
      }
      FileMode mergedMode = mergeMode(entry.getBase().getMode(), entry.getOurs().getMode(), entry.getTheirs().getMode());
      if(mergedMode != null) {
        apply(new GitFileEntry(entry.getOurs().getId(), mergedMode));
        return true;
      }
    }
    return false;
  }

  private void apply(@Nonnull GitFileEntry entry) {

  }

  @Nullable
  private static FileMode mergeMode(@Nonnull FileMode base, @Nonnull FileMode ours, @Nonnull FileMode theirs) {
    if (ours == theirs)
      return ours;
    if (base == ours)
      return theirs.equals(MISSING) ? ours : theirs;
    if (base == theirs)
      return ours.equals(MISSING) ? theirs : ours;
    return null;
  }

  private boolean contentMerge(@Nonnull ThreeWayEntry entry) {
    return true;
  }

}
