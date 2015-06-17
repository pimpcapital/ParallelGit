package com.beijunyi.parallelgit.util.exception;

import javax.annotation.Nonnull;

public class RefUpdateIOFailureException extends RuntimeException {

  public RefUpdateIOFailureException(@Nonnull String name) {
    super(name);
  }

}
