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
    return doExecute();
  }

  @Nonnull
  protected abstract Result doExecute() throws IOException;

  private void checkExecuted() {
    if(executed)
      throw new IllegalStateException("Command already executed");
  }

}
