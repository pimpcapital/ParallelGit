package com.beijunyi.parallelgit.utils.exception;

import javax.annotation.Nonnull;

public class NoSuchBranchException extends RuntimeException {

  private final String refName;

  public NoSuchBranchException(@Nonnull String refName) {
    super("Branch " + refName + " does not exist");
    this.refName = refName;
  }

  @Nonnull
  public String getRefName() {
    return refName;
  }
}
