package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GfsFileStore;
import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import org.eclipse.jgit.lib.Repository;

public abstract class GfsCommand<Result extends GfsCommandResult> {

  protected final GitFileSystem gfs;
  protected final GfsStatusProvider status;
  protected final GfsFileStore store;
  protected final Repository repo;

  protected boolean executed = false;

  protected GfsCommand(GitFileSystem gfs) {
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
      return doExecute(update);
    }
  }

  @Nonnull
  protected abstract Result doExecute(GfsStatusProvider.Update update) throws IOException;

  private void checkExecuted() {
    if(executed)
      throw new IllegalStateException("Command already executed");
  }

}
