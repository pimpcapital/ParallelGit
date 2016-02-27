package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.Repository;

import static com.beijunyi.parallelgit.utils.GitFileUtils.*;

public class ReadBlobHandler extends AbstractRepositoryRequestHandler {

  @Nonnull
  @Override
  public String getType() {
    return "read-blob";
  }

  @Nonnull
  @Override
  protected ServerResponse handle(@Nonnull ClientRequest request, @Nonnull Repository repo) throws IOException {
    String revision = request.getString("revision");
    String path = request.getString("path");
    byte[] data = isFile(path, revision, repo) ? readFile(path, revision, repo).getData() : new byte[] {};
    return request.respond().ok(new String(data));
  }

}
