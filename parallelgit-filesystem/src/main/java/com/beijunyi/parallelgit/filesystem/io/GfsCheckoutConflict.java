package com.beijunyi.parallelgit.filesystem.io;

import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.io.GitFileEntry;

public class GfsCheckoutConflict {

  private final String path;
  private final GitFileEntry head;
  private final GitFileEntry target;
  private final GitFileEntry worktree;

  private GfsCheckoutConflict(String path, GitFileEntry head, GitFileEntry target, GitFileEntry worktree) {
    this.path = path;
    this.head = head;
    this.target = target;
    this.worktree = worktree;
  }

  @Nonnull
  public static GfsCheckoutConflict threeWayConflict(String path, GitFileEntry head, GitFileEntry target, GitFileEntry worktree) {
    return new GfsCheckoutConflict(path, head, target, worktree);
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
