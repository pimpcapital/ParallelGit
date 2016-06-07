package com.beijunyi.parallelgit.utils.exceptions;

public class BranchAlreadyExistsException extends RuntimeException {

  public BranchAlreadyExistsException(String refName) {
    super("Branch " + refName + " already exists");
  }

}
