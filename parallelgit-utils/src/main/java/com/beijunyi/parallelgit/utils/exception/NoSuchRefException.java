package com.beijunyi.parallelgit.utils.exception;

import javax.annotation.Nonnull;

public class NoSuchRefException extends RuntimeException {

  private final String refName;

  public NoSuchRefException(@Nonnull String refName) {
    super("Ref " + refName + " does not exist");
    this.refName = refName;
  }

  @Nonnull
  public String getRefName() {
    return refName;
  }
}
