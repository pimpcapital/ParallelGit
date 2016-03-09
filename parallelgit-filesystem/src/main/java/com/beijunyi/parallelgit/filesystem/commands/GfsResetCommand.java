package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GfsState;
import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.exceptions.NoBranchException;
import com.beijunyi.parallelgit.filesystem.exceptions.NoHeadCommitException;
import org.eclipse.jgit.lib.AnyObjectId;

import static com.beijunyi.parallelgit.filesystem.GfsState.RESETTING;

public class GfsResetCommand extends GfsCommand<GfsResetCommand.Result> {

  private boolean soft = false;
  private AnyObjectId commit;

  public GfsResetCommand(@Nonnull GitFileSystem gfs) {
    super(gfs);
  }

  @Nonnull
  public GfsResetCommand soft(boolean soft) {
    this.soft = soft;
    return this;
  }

  @Nonnull
  @Override
  protected GfsState getCommandState() {
    return RESETTING;
  }

  @Nonnull
  @Override
  protected Result doExecute(@Nonnull GfsStatusProvider.Update update) throws IOException {
    checkAttached();
    prepareCommit();

    return null;
  }

  private void checkAttached() {
    if(!status.isAttached())
      throw new NoBranchException();
  }

  private void prepareCommit() throws IOException {
    if(commit == null) {
      if(!status.isInitialized())
        throw new NoHeadCommitException();
      commit = status.commit();
    }
  }

  public static class Result implements GfsCommandResult {

    @Override
    public boolean isSuccessful() {
      return false;
    }

  }

}
