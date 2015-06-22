package com.beijunyi.parallelgit.commands.cache;

import java.io.IOException;
import javax.annotation.Nonnull;

public abstract class CacheEditor {
  protected final String path;

  protected CacheEditor(@Nonnull String path) {
    this.path = path;
  }

  public abstract void edit(@Nonnull CacheStateProvider provider) throws IOException;
}