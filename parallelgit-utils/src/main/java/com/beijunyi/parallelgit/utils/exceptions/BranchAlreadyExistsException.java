package com.beijunyi.parallelgit.utils.exceptions;

import javax.annotation.Nonnull;

public class BranchAlreadyExistsException extends RuntimeException {

  public BranchAlreadyExistsException(@Nonnull String refName) {
    super("Branch " + refName + " already exists");
  }

}
