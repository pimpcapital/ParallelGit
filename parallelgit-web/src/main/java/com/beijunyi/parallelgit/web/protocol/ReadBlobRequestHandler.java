package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.ObjectUtils;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Repository;

public class ReadBlobRequestHandler extends AbstractRepositoryRequestHandler {

  @Nonnull
  @Override
  public String getType() {
    return "read-blob";
  }

  @Nonnull
  @Override
  protected ServerResponse handle(@Nonnull ClientRequest request, @Nonnull Repository repo) throws IOException {
    AnyObjectId id = repo.resolve(request.getString("id"));
    String data = id != null ? new String(ObjectUtils.readBlob(id, repo).getData()) : "";
    return request.respond().ok(data);
  }

}
