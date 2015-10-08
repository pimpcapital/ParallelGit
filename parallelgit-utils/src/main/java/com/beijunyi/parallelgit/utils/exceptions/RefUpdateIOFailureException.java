package com.beijunyi.parallelgit.utils.exceptions;

import javax.annotation.Nonnull;

public class RefUpdateIOFailureException extends RuntimeException {

  public RefUpdateIOFailureException(@Nonnull String name) {
    super(name);
  }

}
