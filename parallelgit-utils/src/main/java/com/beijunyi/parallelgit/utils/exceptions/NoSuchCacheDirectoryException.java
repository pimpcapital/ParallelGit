package com.beijunyi.parallelgit.utils.exceptions;

import javax.annotation.Nonnull;

public class NoSuchCacheDirectoryException extends RuntimeException {

  public NoSuchCacheDirectoryException(@Nonnull String path) {
    super("Cache directory " + path + " does not exist");
  }

}
