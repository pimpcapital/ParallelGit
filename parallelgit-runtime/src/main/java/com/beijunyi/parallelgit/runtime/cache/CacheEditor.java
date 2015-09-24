package com.beijunyi.parallelgit.runtime.cache;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.TreeUtils;

public abstract class CacheEditor {
  protected final String path;

  protected CacheEditor(@Nonnull String path) {
    this.path = TreeUtils.normalizeTreePath(path);
  }

  public abstract void edit(@Nonnull CacheStateProvider provider) throws IOException;
}