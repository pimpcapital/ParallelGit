package com.beijunyi.parallelgit.filesystem.io;

import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.io.GitFileEntry;

import static com.beijunyi.parallelgit.filesystem.utils.GfsPathUtils.toAbsolutePath;

public class GfsCheckoutConflict {

  private final String path;
  private final String name;
  private final int depth;
  private final GitFileEntry head;
  private final GitFileEntry target;
  private final GitFileEntry worktree;

  public GfsCheckoutConflict(@Nonnull String path, @Nonnull String name, int depth, @Nonnull GitFileEntry head, @Nonnull GitFileEntry target, @Nonnull GitFileEntry worktree) {
    this.path = toAbsolutePath(path);
    this.name = name;
    this.depth = depth;
    this.head = head;
    this.target = target;
    this.worktree = worktree;
  }

  @Nonnull
  public String getPath() {
    return path;
  }

  @Nonnull
  public String getName() {
    return name;
  }

  public int getDepth() {
    return depth;
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
