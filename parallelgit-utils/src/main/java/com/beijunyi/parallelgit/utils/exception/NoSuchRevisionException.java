package com.beijunyi.parallelgit.utils.exception;

import javax.annotation.Nonnull;

public class NoSuchRevisionException extends RuntimeException {

  public NoSuchRevisionException(@Nonnull String revision) {
    super("Revision " + revision + " does not exist");
  }

}
