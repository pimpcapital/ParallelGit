package com.beijunyi.parallelgit.filesystem.requests;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;

public abstract class GitFileSystemRequest<Result> {

  protected final GitFileSystem gfs;
  protected final Repository repo;
  protected volatile boolean executed = false;

  private RevWalk revWalk;

  protected GitFileSystemRequest(@Nonnull GitFileSystem gfs) {
    this.gfs = gfs;
    this.repo = gfs.getRepository();
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

  @Nullable
  protected abstract Result doExecute() throws IOException;

  private void checkExecuted() {
    if(executed)
      throw new IllegalStateException("Request already executed");
  }

}
