package com.beijunyi.parallelgit.util.builder;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.Repository;

public final class ParallelCacheBuilder extends CacheBasedBuilder<ParallelCacheBuilder, DirCache> {

  private ParallelCacheBuilder(@Nullable Repository repository) {
    super(repository);
  }

  @Nonnull
  @Override
  protected ParallelCacheBuilder self() {
    return this;
  }

  @Nonnull
  @Override
  protected DirCache doBuild() throws IOException {
    return buildCache();
  }

  @Nonnull
  @Override
  public DirCache build() throws IOException {
    DirCache cache = super.build();
    assert cache != null;
    return cache;
  }

  @Nonnull
  public static ParallelCacheBuilder prepare(@Nullable Repository repository) {
    return new ParallelCacheBuilder(repository);
  }

  @Nonnull
  public static ParallelCacheBuilder prepare() {
    return prepare(null);
  }

}
