package com.beijunyi.parallelgit.filesystem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.revwalk.RevCommit;

public class GfsStatus {

  private final String branch;
  private final RevCommit head;
  private final GfsState state;

  public GfsStatus(@Nullable String branch, @Nullable RevCommit head, @Nonnull GfsState state) {
    this.branch = branch;
    this.head = head;
    this.state = state;
  }

  @Nullable
  public String getBranch() {
    return branch;
  }

  @Nullable
  public RevCommit getHead() {
    return head;
  }

  @Nonnull
  public GfsState getState() {
    return state;
  }

}
