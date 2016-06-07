package com.beijunyi.parallelgit.utils.exceptions;

public class NoSuchCommitException extends RuntimeException {

  public NoSuchCommitException(String revision) {
    super("Revision " + revision + " does not exist");
  }

}
