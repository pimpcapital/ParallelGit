package com.beijunyi.parallelgit.filesystem.merge;

import java.util.*;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.io.GfsChanges;

import static java.util.Collections.*;

public class GfsMergeChanges extends GfsChanges {

  private final Set<String> failedPaths = new HashSet<>();
  private final Map<String, GfsMergeConflict> conflicts = new HashMap<>();

  public void addFailedPath(@Nonnull String path) {
    failedPaths.add(path);
  }

  public boolean hasFailedPaths() {
    return !failedPaths.isEmpty();
  }

  @Nonnull
  public Set<String> getFailedPaths() {
    return unmodifiableSet(failedPaths);
  }

  public void addConflict(@Nonnull GfsMergeConflict conflict) {
    conflicts.put(conflict.getPath(), conflict);
  }

  public boolean hasConflicts() {
    return !conflicts.isEmpty();
  }

  @Nonnull
  public Map<String, GfsMergeConflict> getConflicts() {
    return unmodifiableMap(conflicts);
  }
}
