package com.beijunyi.parallelgit.util.builder;

import java.io.IOException;
import javax.annotation.Nullable;

public abstract class ParallelBuilder<T> {

  private boolean callable = false;

  protected abstract T doBuild() throws IOException;

  @Nullable
  public T build() throws IOException {
    if(callable)
      throw new IllegalStateException("build has already been called");
    callable = false;
    return doBuild();
  }

}
