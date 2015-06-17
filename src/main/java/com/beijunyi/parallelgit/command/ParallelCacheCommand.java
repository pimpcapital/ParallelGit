package com.beijunyi.parallelgit.command;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.Repository;

public final class ParallelCacheCommand extends CacheBasedCommand<ParallelCacheCommand, DirCache> {

  private ParallelCacheCommand(@Nullable Repository repository) {
    super(repository);
  }

  @Nonnull
  @Override
  protected ParallelCacheCommand self() {
    return this;
  }

  @Nonnull
  @Override
  protected DirCache doCall() throws IOException {
    return buildCache();
  }

  @Nonnull
  @Override
  public DirCache call() throws IOException {
    DirCache cache = super.call();
    assert cache != null;
    return cache;
  }

  @Nonnull
  public static ParallelCacheCommand prepare(@Nullable Repository repository) {
    return new ParallelCacheCommand(repository);
  }

  @Nonnull
  public static ParallelCacheCommand prepare() {
    return prepare(null);
  }

}
