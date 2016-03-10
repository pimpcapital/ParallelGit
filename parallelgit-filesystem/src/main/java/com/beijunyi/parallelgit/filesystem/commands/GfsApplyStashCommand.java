package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GfsState;
import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;

import static com.beijunyi.parallelgit.filesystem.GfsState.APPLYING_STASH;

public class GfsApplyStashCommand extends GfsCommand<GfsCreateStashCommand.Result> {

  public GfsApplyStashCommand(@Nonnull GitFileSystem gfs) {
    super(gfs);
  }

  @Nonnull
  @Override
  protected GfsState getCommandState() {
    return APPLYING_STASH;
  }

  @Nonnull
  @Override
  protected GfsCreateStashCommand.Result doExecute(@Nonnull GfsStatusProvider.Update update) throws IOException {
    return null;
  }

}
