package com.beijunyi.parallelgit.filesystem.merge;

import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.io.GitFileEntry;
import org.eclipse.jgit.treewalk.TreeWalk;

public class QuadWayEntry {

  public static final int BASE = 0;
  public static final int HEAD = 1;
  public static final int TARGET = 2;
  public static final int WORKTREE = 3;

  private final String path;
  private final String name;
  private final int depth;
  private final GitFileEntry base;
  private final GitFileEntry head;
  private final GitFileEntry target;
  private final GitFileEntry worktree;

  public QuadWayEntry(@Nonnull String path, @Nonnull String name, int depth, @Nonnull GitFileEntry base, @Nonnull GitFileEntry head, @Nonnull GitFileEntry target, @Nonnull GitFileEntry worktree) {
    this.path = path;
    this.name = name;
    this.depth = depth;
    this.base = base;
    this.head = head;
    this.target = target;
    this.worktree = worktree;
  }

  @Nonnull
  public static QuadWayEntry read(@Nonnull TreeWalk tw) {
    return new QuadWayEntry(tw.getPathString(), tw.getNameString(), tw.getDepth(),
                             GitFileEntry.forTreeNode(tw, BASE),
                             GitFileEntry.forTreeNode(tw, HEAD),
                             GitFileEntry.forTreeNode(tw, TARGET),
                             GitFileEntry.forTreeNode(tw, WORKTREE)
    );
  }

  @Nonnull
  public String path() {
    return path;
  }

  @Nonnull
  public String name() {
    return name;
  }

  public int depth() {
    return depth;
  }

  @Nonnull
  public GitFileEntry base() {
    return base;
  }

  @Nonnull
  public GitFileEntry head() {
    return head;
  }

  @Nonnull
  public GitFileEntry target() {
    return target;
  }

  @Nonnull
  public GitFileEntry worktree() {
    return worktree;
  }

  public boolean isDirty() {
    return !head.equals(worktree);
  }

}
