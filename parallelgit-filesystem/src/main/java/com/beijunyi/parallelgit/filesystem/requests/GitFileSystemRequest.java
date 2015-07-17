package com.beijunyi.parallelgit.filesystem.requests;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;

public abstract class GitFileSystemRequest<Request extends GitFileSystemRequest, Result> {

  protected GitFileSystem gfs;
  protected volatile boolean executed = false;

  @Nonnull
  protected abstract Request self();

  @Nullable
  protected abstract Result doExecute() throws IOException;

  @Nonnull
  public Request gfs(@Nonnull GitFileSystem gfs) {
    this.gfs = gfs;
    return self();
  }

  protected void ensureFileSystem() {
    if(gfs == null)
      throw new IllegalArgumentException("Missing file system");
  }

  private void checkExecuted() {
    if(executed)
      throw new IllegalStateException("Request already executed");
  }

  @Nullable
  public Result execute() throws IOException {
    checkExecuted();
    synchronized(this) {
      checkExecuted();
      ensureFileSystem();
      executed = true;
      return doExecute();
    }
  }

}
