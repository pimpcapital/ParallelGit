package com.beijunyi.parallelgit.filesystem.requests;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import org.eclipse.jgit.lib.Repository;

public abstract class GitFileSystemRequest<Result> {

  protected final GitFileSystem gfs;
  protected final Repository repository;
  protected volatile boolean executed = false;

  protected GitFileSystemRequest(@Nonnull GitFileSystem gfs) {
    this.gfs = gfs;
    this.repository = gfs.getRepository();
  }

  @Nullable
  protected abstract Result doExecute() throws IOException;

  private void checkExecuted() {
    if(executed)
      throw new IllegalStateException("Request already executed");
  }

  @Nullable
  public Result execute() throws IOException {
    checkExecuted();
    synchronized(this) {
      checkExecuted();
      executed = true;
      return doExecute();
    }
  }

}
