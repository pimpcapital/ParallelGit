package com.beijunyi.parallelgit.util.builder;

import java.io.IOException;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.Repository;

public abstract class ParallelBuilder<T> {

  protected final Repository repository;
  private boolean callable = false;

  protected ParallelBuilder(Repository repository) {
    this.repository = repository;
  }

  protected abstract T doBuild() throws IOException;

  @Nullable
  public T build() throws IOException {
    if(callable)
      throw new IllegalStateException("build has already been called");
    callable = false;
    return doBuild();
  }

}
