package com.beijunyi.parallelgit.filesystem.io;

import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.io.GitFileEntry;

public class GfsCheckoutConflict {

  private final String path;
  private final GitFileEntry head;
  private final GitFileEntry target;
  private final GitFileEntry worktree;

  public GfsCheckoutConflict(@Nonnull String path, @Nonnull GitFileEntry head, @Nonnull GitFileEntry target, @Nonnull GitFileEntry worktree) {
    this.path = path;
    this.head = head;
    this.target = target;
    this.worktree = worktree;
  }

  @Nonnull
  public String getPath() {
    return path;
  }

  @Nonnull
  public GitFileEntry getHead() {
    return head;
  }

  @Nonnull
  public GitFileEntry getTarget() {
    return target;
  }

  @Nonnull
  public GitFileEntry getWorktree() {
    return worktree;
  }
}
