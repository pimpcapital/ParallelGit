package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;

public final class GfsCheckoutCommand extends GfsCommand<GfsCheckoutCommand.Result> {

  public GfsCheckoutCommand(@Nonnull GitFileSystem gfs) {
    super(gfs);
  }

  @Nonnull
  @Override
  protected Result doExecute() throws IOException {
    return null;
  }

  public static class Result implements GfsCommandResult {

    @Override
    public boolean isSuccessful() {
      return false;
    }

  }

}
