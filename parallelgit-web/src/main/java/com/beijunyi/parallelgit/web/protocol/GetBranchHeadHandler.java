package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.BranchUtils;
import com.beijunyi.parallelgit.web.protocol.model.CommitView;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

public class GetBranchHeadHandler extends AbstractRepositoryRequestHandler {

  @Nonnull
  @Override
  public String getType() {
    return "get-branch-head";
  }

  @Nonnull
  @Override
  protected ServerResponse handle(@Nonnull ClientRequest request, @Nonnull Repository repo) throws IOException {
    String name = request.getString("name");
    RevCommit commit = BranchUtils.getHeadCommit(name, repo);
    CommitView view = CommitView.of(commit);
    return request.respond().ok(view);
  }
}
