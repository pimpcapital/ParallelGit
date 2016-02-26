package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.web.workspace.Workspace;

public class CommitHandler extends AbstractGfsRequestHandler {

  @Nonnull
  @Override
  public String getType() {
    return "commit";
  }

  @Nonnull
  @Override
  public ServerResponse handle(@Nonnull ClientRequest request, @Nonnull GitFileSystem gfs) throws IOException {
    throw new UnsupportedOperationException();
  }
}
