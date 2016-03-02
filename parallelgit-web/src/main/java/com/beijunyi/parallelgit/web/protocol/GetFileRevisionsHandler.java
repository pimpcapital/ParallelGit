package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.CommitUtils;
import com.beijunyi.parallelgit.web.protocol.model.CommitView;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
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
    List<CommitView> views = Lists.transform(commits, new Function<RevCommit, CommitView>() {
      @Nonnull
      @Override
      public CommitView apply(@Nullable RevCommit revCommit) {
        assert revCommit != null;
        return CommitView.of(revCommit);
      }
    });
    return request.respond().ok(views);
  }
}
