package com.beijunyi.parallelgit.utils.exceptions;

public class NoSuchCacheEntryException extends RuntimeException {

  public NoSuchCacheEntryException(String path) {
    super("Cache entry " + path + " does not exist");
  }

}
