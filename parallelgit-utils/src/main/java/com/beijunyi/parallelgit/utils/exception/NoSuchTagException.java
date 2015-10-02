package com.beijunyi.parallelgit.utils.exception;

import javax.annotation.Nonnull;

public class NoSuchTagException extends RuntimeException {

  private final String refName;

  public NoSuchTagException(@Nonnull String refName) {
    super("Tag " + refName + " does not exist");
    this.refName = refName;
  }

  @Nonnull
  public String getRefName() {
    return refName;
  }
}
