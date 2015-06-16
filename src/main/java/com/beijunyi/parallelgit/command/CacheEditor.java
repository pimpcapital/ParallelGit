package com.beijunyi.parallelgit.command;

import java.io.IOException;
import javax.annotation.Nonnull;

abstract class CacheEditor {
  protected final String path;

  protected CacheEditor(@Nonnull String path) {
    this.path = path;
  }

  protected abstract void doEdit(@Nonnull BuildStateProvider provider) throws IOException;
}