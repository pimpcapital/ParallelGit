package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.*;
import org.eclipse.jgit.lib.Repository;

public abstract class GfsCommand<Result extends GfsCommandResult> {

  protected final GitFileSystem gfs;
  protected final Repository repo;

  protected boolean executed = false;

  protected GfsCommand(@Nonnull GitFileSystem gfs) {
    this.gfs = gfs;
    this.repo = gfs.getRepository();
  }

  @Nonnull
  public synchronized Result execute() throws IOException {
    checkExecuted();
    executed = true;

    GfsStatusProvider status = gfs.status();
    GfsState startState = startState(status.state());
    GfsStatusUpdater update = status.prepareUpdate(startState);

    Result result = doExecute(update);

    GfsState endState = exitState(result);
    status.completeUpdate(endState, update);
    return result;
  }

  @Nonnull
  protected abstract GfsState startState(@Nonnull GfsState current);

  @Nonnull
  protected abstract GfsState exitState(@Nonnull Result result);

  @Nonnull
  protected abstract Result doExecute(@Nonnull GfsStatusUpdater status) throws IOException;

  private void checkExecuted() {
    if(executed)
      throw new IllegalStateException("Command already executed");
  }

}
