package com.beijunyi.parallelgit.utils.exceptions;

public class RefUpdateIOFailureException extends RuntimeException {

  public RefUpdateIOFailureException(String name) {
    super(name);
  }

}
