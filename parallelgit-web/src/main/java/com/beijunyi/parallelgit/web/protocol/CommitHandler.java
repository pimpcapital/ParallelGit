package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.web.workspace.Workspace;

public class CommitHandler implements RequestHandler {

  @Override
  public String getType() {
    return "commit";
  }

  @Nonnull
  @Override
  public ServerResponse handle(@Nonnull ClientRequest request, @Nonnull Workspace workspace) throws IOException {
    throw new UnsupportedOperationException();
  }
}
