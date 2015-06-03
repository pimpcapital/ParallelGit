package com.beijunyi.parallelgit.util.builder;

import java.io.IOException;
import javax.annotation.Nonnull;

import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.Repository;

public class ParallelCacheBuilder extends CacheBasedBuilder<ParallelCacheBuilder, DirCache> {

  private ParallelCacheBuilder(@Nonnull Repository repository) {
    super(repository);
  }

  @Nonnull
  @Override
  protected ParallelCacheBuilder self() {
    return this;
  }

  @Override
  protected DirCache doBuild() throws IOException {
    return buildCache(repository);
  }

  @Nonnull
  public static ParallelCacheBuilder prepare(@Nonnull Repository repository) {
    return new ParallelCacheBuilder(repository);
  }

}
