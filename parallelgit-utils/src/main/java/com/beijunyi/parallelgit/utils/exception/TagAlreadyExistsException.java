package com.beijunyi.parallelgit.utils.exception;

import javax.annotation.Nonnull;

public class TagAlreadyExistsException extends RuntimeException {

  public TagAlreadyExistsException(@Nonnull String refName) {
    super("Tag " + refName + " already exists");
  }

}
