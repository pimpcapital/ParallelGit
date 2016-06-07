package com.beijunyi.parallelgit.filesystem.io;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.exceptions.GfsCheckoutConflictException;

import static java.util.Collections.unmodifiableMap;

public class GfsCheckoutChangesCollector extends GfsChangesCollector {

  private final Map<String, GfsCheckoutConflict> conflicts = new HashMap<>();
  private final boolean failsOnConflict;

  public GfsCheckoutChangesCollector(boolean failsOnConflict) {
    this.failsOnConflict = failsOnConflict;
  }

  public void addConflict(GfsCheckoutConflict conflict) {
    conflicts.put(conflict.getPath(), conflict);
    if(failsOnConflict)
      throw new GfsCheckoutConflictException(conflict);
  }

  public boolean hasConflicts() {
    return !conflicts.isEmpty();
  }

  @Nonnull
  public Map<String, GfsCheckoutConflict> getConflicts() {
    return unmodifiableMap(conflicts);
  }

}
