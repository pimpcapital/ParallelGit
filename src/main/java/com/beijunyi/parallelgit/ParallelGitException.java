package com.beijunyi.parallelgit;

public class ParallelGitException extends RuntimeException {

  public ParallelGitException() {
  }

  public ParallelGitException(String message) {
    super(message);
  }

  public ParallelGitException(String message, Throwable cause) {
    super(message, cause);
  }

  public ParallelGitException(Throwable cause) {
    super(cause);
  }

  public ParallelGitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
