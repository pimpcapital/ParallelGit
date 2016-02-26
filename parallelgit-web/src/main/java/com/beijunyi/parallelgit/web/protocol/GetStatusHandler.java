package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.web.protocol.model.Head;
import com.beijunyi.parallelgit.web.protocol.model.Status;

public class GetStatusHandler extends AbstractGfsRequestHandler {

  @Nonnull
  @Override
  public String getType() {
    return "get-status";
  }

  @Nonnull
  @Override
  public ServerResponse handle(@Nonnull ClientRequest request, @Nonnull GitFileSystem gfs) throws IOException {
    return request.respond().ok(Status.of(gfs));
  }
}
