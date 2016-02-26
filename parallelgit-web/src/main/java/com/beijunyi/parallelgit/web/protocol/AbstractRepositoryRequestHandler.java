package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.web.workspace.Workspace;
import org.eclipse.jgit.lib.Repository;

public abstract class AbstractRepositoryRequestHandler implements RequestHandler {

  @Nonnull
  @Override
  public ServerResponse handle(@Nonnull ClientRequest request, @Nonnull Workspace workspace) throws IOException {
    Repository repo = workspace.getRepository();
    if(repo == null)
      throw new IllegalStateException();
    return handle(request, repo);
  }

  @Nonnull
  protected abstract ServerResponse handle(@Nonnull ClientRequest request, @Nonnull Repository repo) throws IOException;

}
