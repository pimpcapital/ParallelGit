package com.beijunyi.parallelgit.utils.exception;

import javax.annotation.Nonnull;

public class BranchAlreadyExistsException extends RuntimeException {

  private final String refName;

  public BranchAlreadyExistsException(@Nonnull String refName) {
    super("Branch " + refName + " already exists");
    this.refName = refName;
  }

  @Nonnull
  public String getRefName() {
    return refName;
  }
}
