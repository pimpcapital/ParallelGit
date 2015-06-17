package com.beijunyi.parallelgit.util.exception;

import javax.annotation.Nonnull;

public class RefUpdateLockFailureException extends RuntimeException {

  public RefUpdateLockFailureException(@Nonnull String message) {
    super(message);
  }

}
