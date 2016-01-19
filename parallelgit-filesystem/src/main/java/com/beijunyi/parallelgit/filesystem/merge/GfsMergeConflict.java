package com.beijunyi.parallelgit.filesystem.merge;

import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.io.GfsCheckoutConflict;
import com.beijunyi.parallelgit.utils.io.GitFileEntry;

public class GfsMergeConflict extends GfsCheckoutConflict {

  private final GitFileEntry base;

  public GfsMergeConflict(@Nonnull String path, @Nonnull String name, int depth, @Nonnull GitFileEntry base, @Nonnull GitFileEntry head, @Nonnull GitFileEntry target, @Nonnull GitFileEntry worktree) {
    super(path, name, depth, head, target, worktree);
    this.base = base;
  }

  @Nonnull
  public GitFileEntry getBase() {
    return base;
  }
}
