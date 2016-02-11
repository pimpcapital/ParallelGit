package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.web.workspace.Workspace;

public interface RequestHandler {

  String getType();

  @Nonnull
  ServerResponse handle(@Nonnull ClientRequest request, @Nonnull Workspace workspace) throws IOException;

}
