package com.beijunyi.parallelgit.filesystem.merge;

import java.util.*;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.io.GfsChangesCollector;

import static java.util.Collections.*;

public class GfsMergeChangesCollector extends GfsChangesCollector {

  private final Set<String> failedPaths = new HashSet<>();
  private final Map<String, QuadWayEntry> conflicts = new HashMap<>();

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

  public void addConflict(@Nonnull QuadWayEntry conflict) {
    conflicts.put(conflict.path(), conflict);
  }

  public boolean hasConflicts() {
    return !conflicts.isEmpty();
  }

  @Nonnull
  public Map<String, QuadWayEntry> getConflicts() {
    return unmodifiableMap(conflicts);
  }
}
