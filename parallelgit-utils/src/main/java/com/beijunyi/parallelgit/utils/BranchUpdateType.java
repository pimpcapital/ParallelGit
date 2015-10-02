package com.beijunyi.parallelgit.utils;

import javax.annotation.Nonnull;

public enum BranchUpdateType {
  COMMIT("commit", false),
  COMMIT_AMEND("commit (amend)", true),
  COMMIT_INIT("commit (initial)", false),
  CHERRY_PICK("cherry-pick", false);

  private final String header;
  private final boolean forceUpdate;

  BranchUpdateType(@Nonnull String action, boolean forceUpdate) {
    this.header = action + ": ";
    this.forceUpdate = forceUpdate;
  }

  @Nonnull
  public String getHeader() {
    return header;
  }

  public boolean isForceUpdate() {
    return forceUpdate;
  }
}
