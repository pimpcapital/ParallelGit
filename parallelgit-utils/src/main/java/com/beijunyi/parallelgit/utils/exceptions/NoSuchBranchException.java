package com.beijunyi.parallelgit.utils.exceptions;

public class NoSuchBranchException extends RuntimeException {

  public NoSuchBranchException(String refName) {
    super("Branch " + refName + " does not exist");
  }

}
