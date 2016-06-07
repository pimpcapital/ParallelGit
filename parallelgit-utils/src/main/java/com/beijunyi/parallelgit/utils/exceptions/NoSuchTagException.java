package com.beijunyi.parallelgit.utils.exceptions;

public class NoSuchTagException extends RuntimeException {

  public NoSuchTagException(String refName) {
    super("Tag " + refName + " does not exist");
  }

}
