package com.beijunyi.parallelgit.utils.exception;

import javax.annotation.Nonnull;

public class NoSuchRevisionException extends RuntimeException {

  private final String revision;

  public NoSuchRevisionException(@Nonnull String revision) {
    super("Revision " + revision + " does not exist");
    this.revision = revision;
  }

  @Nonnull
  public String getRevision() {
    return revision;
  }
}
