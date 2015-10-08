package com.beijunyi.parallelgit.utils.exceptions;

import javax.annotation.Nonnull;

public class NoSuchTagException extends RuntimeException {

  public NoSuchTagException(@Nonnull String refName) {
    super("Tag " + refName + " does not exist");
  }

}
