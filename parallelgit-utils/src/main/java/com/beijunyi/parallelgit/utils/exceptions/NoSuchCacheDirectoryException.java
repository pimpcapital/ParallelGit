package com.beijunyi.parallelgit.utils.exceptions;

public class NoSuchCacheDirectoryException extends RuntimeException {

  public NoSuchCacheDirectoryException(String path) {
    super("Cache directory " + path + " does not exist");
  }

}
