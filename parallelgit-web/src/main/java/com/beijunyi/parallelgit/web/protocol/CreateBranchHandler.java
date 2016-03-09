package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.BranchUtils;
import org.eclipse.jgit.lib.Repository;

public class CreateBranchHandler extends AbstractRepositoryRequestHandler {

  @Nonnull
  @Override
  public String getType() {
    return "create-branch";
  }

  @Nonnull
  @Override
  protected ServerResponse handle(@Nonnull ClientRequest request, @Nonnull Repository repo) throws IOException {
    String name = request.getString("name");
    String start = request.getString("start");
    BranchUtils.createBranch(name, start, repo);
    return request.respond().ok();
  }
}
