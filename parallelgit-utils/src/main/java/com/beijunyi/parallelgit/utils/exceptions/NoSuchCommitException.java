package com.beijunyi.parallelgit.utils.exceptions;

import javax.annotation.Nonnull;

public class NoSuchCommitException extends RuntimeException {

  public NoSuchCommitException(@Nonnull String revision) {
    super("Revision " + revision + " does not exist");
  }

}
