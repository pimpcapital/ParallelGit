package com.beijunyi.parallelgit.utils.exception;

import javax.annotation.Nonnull;

public class RefUpdateLockFailureException extends RuntimeException {

  public RefUpdateLockFailureException(@Nonnull String message) {
    super(message);
  }

}
