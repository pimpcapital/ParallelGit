package com.beijunyi.parallelgit.gfs;

import com.beijunyi.parallelgit.ParallelGitException;

public class GitFileSystemException extends ParallelGitException {

  public GitFileSystemException(String message) {
    super(message);
  }

  public GitFileSystemException(String message, Throwable cause) {
    super(message, cause);
  }

}
