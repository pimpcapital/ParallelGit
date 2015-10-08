package com.beijunyi.parallelgit.utils.exceptions;

import javax.annotation.Nonnull;

public class NoSuchCacheEntryException extends RuntimeException {

  public NoSuchCacheEntryException(@Nonnull String path) {
    super("Cache entry " + path + " does not exist");
  }

}
