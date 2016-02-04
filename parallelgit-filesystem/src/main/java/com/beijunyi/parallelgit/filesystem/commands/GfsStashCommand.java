package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GfsState;
import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;

public class GfsStashCommand extends GfsCommand<GfsStashCommand.Result> {

  public GfsStashCommand(@Nonnull GitFileSystem gfs) {
    super(gfs);
  }

  @Nonnull
  @Override
  protected GfsState getCommandState() {
    return GfsState.STASHING;
  }

  @Nonnull
  @Override
  protected GfsStashCommand.Result doExecute(@Nonnull GfsStatusProvider.Update update) throws IOException {
    return null;
  }

  public static class Result implements GfsCommandResult {

    @Override
    public boolean isSuccessful() {
      return false;
    }
  }

}
