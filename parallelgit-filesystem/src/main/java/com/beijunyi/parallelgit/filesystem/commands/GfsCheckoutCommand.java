package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GfsState;
import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.exceptions.BadGfsStateException;

import static com.beijunyi.parallelgit.filesystem.GfsState.MERGING;
import static com.beijunyi.parallelgit.filesystem.GfsState.NORMAL;

public final class GfsCheckoutCommand extends GfsCommand<GfsCheckoutCommand.Result> {

  public GfsCheckoutCommand(@Nonnull GitFileSystem gfs) {
    super(gfs);
  }

  @Nonnull
  @Override
  protected Result doExecute(@Nonnull GfsStatusProvider.Update update) throws IOException {
    return null;
  }

  @Nonnull
  @Override
  protected GfsState getCommandState() {
    return GfsState.CHECKING_OUT;
  }

  public static class Result implements GfsCommandResult {
    @Override
    public boolean isSuccessful() {
      return false;
    }
  }

}
