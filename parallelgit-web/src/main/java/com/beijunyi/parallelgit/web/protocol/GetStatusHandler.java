package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.web.workspace.Workspace;

public class GetStatusHandler implements RequestHandler {

  @Override
  public String getType() {
    return "get-status";
  }

  @Nonnull
  @Override
  public ServerResponse handle(@Nonnull ClientRequest request, @Nonnull Workspace workspace) throws IOException {
    return request.respond().ok(workspace.getStatus());
  }
}
