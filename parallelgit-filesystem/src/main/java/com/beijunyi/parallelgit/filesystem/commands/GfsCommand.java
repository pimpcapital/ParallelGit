package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsState;
import com.beijunyi.parallelgit.filesystem.GfsStatusUpdate;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import org.eclipse.jgit.lib.Repository;

public abstract class GfsCommand<Result extends GfsCommandResult> {

  protected final GitFileSystem gfs;
  protected final Repository repo;

  protected boolean executed = false;

  protected GfsCommand(@Nonnull GitFileSystem gfs) {
    this.gfs = gfs;
    this.repo = gfs.getRepository();
  }

  @Nullable
  public synchronized Result execute() throws IOException {
    checkExecuted();
    try(GfsStatusUpdate status = gfs.status().prepareUpdate(startState())) {
      executed = true;
      return doExecute(status);
    }
  }

  @Nonnull
  protected abstract GfsState startState();

  @Nonnull
  protected abstract Result doExecute(@Nonnull GfsStatusUpdate status) throws IOException;

  private void checkExecuted() {
    if(executed)
      throw new IllegalStateException("Command already executed");
  }

}
