package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.web.protocol.model.Head;

public class GetHeadHandler extends AbstractGfsRequestHandler {

  @Nonnull
  @Override
  public String getType() {
    return "get-head";
  }

  @Nonnull
  @Override
  public ServerResponse handle(@Nonnull ClientRequest request, @Nonnull GitFileSystem gfs) throws IOException {
    return request.respond().ok(Head.of(gfs));
  }
}
