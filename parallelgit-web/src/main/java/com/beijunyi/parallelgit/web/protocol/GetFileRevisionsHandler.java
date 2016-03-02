package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import java.util.List;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.CommitUtils;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

public class GetFileRevisionsHandler extends AbstractRepositoryRequestHandler {

  @Nonnull
  @Override
  public String getType() {
    return "get-file-revisions";
  }

  @Nonnull
  @Override
  protected ServerResponse handle(@Nonnull ClientRequest request, @Nonnull Repository repo) throws IOException {
    String path = request.getString("path");
    List<RevCommit> commits = CommitUtils.getFileRevisions(path, repo);
    return request.respond().ok(commits);
  }
}
