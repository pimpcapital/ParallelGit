package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.web.workspace.Workspace;

public abstract class AbstractGfsRequestHandler implements RequestHandler {

  @Nonnull
  @Override
  public ServerResponse handle(@Nonnull ClientRequest request, @Nonnull Workspace workspace) throws IOException {
    GitFileSystem gfs = workspace.getFileSystem();
    if(gfs == null)
      throw new IllegalStateException();
    return handle(request, gfs);
  }

  @Nonnull
  protected abstract ServerResponse handle(@Nonnull ClientRequest request, @Nonnull GitFileSystem gfs) throws IOException;


}
