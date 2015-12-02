package com.beijunyi.parallelgit.filesystem.merge;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;

public class GfsMergeResult {

  private final Map<String, GfsMergeConflict> conflicts;
  private final AnyObjectId tree;

  public GfsMergeResult(@Nonnull Map<String, GfsMergeConflict> conflicts, @Nullable AnyObjectId tree) {
    this.conflicts = conflicts;
    this.tree = tree;
  }

  public boolean isSuccessful() {
    return false;
  }

  @Nonnull
  public AnyObjectId getTreeId() {
    return tree;
  }

}
