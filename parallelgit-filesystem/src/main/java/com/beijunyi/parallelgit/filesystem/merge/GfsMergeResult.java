package com.beijunyi.parallelgit.filesystem.merge;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;

public class GfsMergeResult {

  private final boolean successful;
  private final Map<String, GfsMergeConflict> conflicts;
  private final AnyObjectId tree;

  private GfsMergeResult(boolean successful, @Nullable Map<String, GfsMergeConflict> conflicts, @Nullable AnyObjectId tree) {
    this.successful = successful;
    this.conflicts = conflicts;
    this.tree = tree;
  }

  @Nonnull
  public static GfsMergeResult success(@Nonnull AnyObjectId tree) {
    return new GfsMergeResult(true, null, tree);
  }

  @Nonnull
  public static GfsMergeResult conflicting(@Nonnull Map<String, GfsMergeConflict> conflicts) {
    return new GfsMergeResult(false, conflicts, null);
  }

  public boolean isSuccessful() {
    return successful;
  }

  @Nonnull
  public Map<String, GfsMergeConflict> getConflicts() {
    if(conflicts == null)
      throw new IllegalStateException();
    return conflicts;
  }

  @Nonnull
  public AnyObjectId getTree() {
    if(tree == null)
      throw new IllegalStateException();
    return tree;
  }
}
