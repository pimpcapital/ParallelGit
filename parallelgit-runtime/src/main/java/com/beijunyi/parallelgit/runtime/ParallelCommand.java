package com.beijunyi.parallelgit.runtime;

import java.io.IOException;
import javax.annotation.Nullable;

public abstract class ParallelCommand<T> {

  private boolean callable = true;

  protected abstract T doCall() throws IOException;

  @Nullable
  public T call() throws IOException {
    if(!callable)
      throw new IllegalStateException("Command has already been called");
    callable = false;
    return doCall();
  }

}
