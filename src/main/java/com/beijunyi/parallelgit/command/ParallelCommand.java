package com.beijunyi.parallelgit.command;

import java.io.IOException;
import javax.annotation.Nullable;

public abstract class ParallelCommand<T> {

  private boolean callable = false;

  protected abstract T doCall() throws IOException;

  @Nullable
  public T call() throws IOException {
    if(callable)
      throw new IllegalStateException("command has already been called");
    callable = false;
    return doCall();
  }

}
