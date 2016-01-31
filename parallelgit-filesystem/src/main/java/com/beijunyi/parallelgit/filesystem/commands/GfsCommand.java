package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import java.util.EnumSet;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.*;
import com.beijunyi.parallelgit.filesystem.exceptions.BadGfsStateException;
import org.eclipse.jgit.lib.Repository;

public abstract class GfsCommand<Result extends GfsCommandResult> {

  protected final GitFileSystem gfs;
  protected final GfsStatusProvider status;
  protected final GfsFileStore store;
  protected final Repository repo;

  protected boolean executed = false;

  protected GfsCommand(@Nonnull GitFileSystem gfs) {
    this.gfs = gfs;
    this.status = gfs.getStatusProvider();
    this.store = gfs.getFileStore();
    this.repo = gfs.getRepository();
  }

  @Nonnull
  public synchronized Result execute() throws IOException {
    checkExecuted();
    executed = true;
    try(GfsStatusProvider.Update update = status.prepareUpdate()) {
      prepareState(update);
      return doExecute(update);
    }
  }

  protected void prepareState(@Nonnull GfsStatusProvider.Update update) {
    if(!getAcceptableStates().contains(status.state()))
      throw new BadGfsStateException(status.state());
    update.state(getCommandState());
  }

  @Nonnull
  protected EnumSet<GfsState> getAcceptableStates() {
     return EnumSet.of(GfsState.NORMAL);
  }

  @Nonnull
  protected abstract GfsState getCommandState();

  @Nonnull
  protected abstract Result doExecute(@Nonnull GfsStatusProvider.Update update) throws IOException;

  private void checkExecuted() {
    if(executed)
      throw new IllegalStateException("Command already executed");
  }

}
