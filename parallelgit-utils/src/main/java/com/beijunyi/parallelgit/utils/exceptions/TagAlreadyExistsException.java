package com.beijunyi.parallelgit.utils.exceptions;

public class TagAlreadyExistsException extends RuntimeException {

  public TagAlreadyExistsException(String refName) {
    super("Tag " + refName + " already exists");
  }

}
