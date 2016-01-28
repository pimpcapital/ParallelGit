package com.beijunyi.parallelgit.web.workspace;

import java.io.IOException;
import java.util.Set;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.BranchUtils;
import org.eclipse.jgit.lib.Repository;

public class Workspace {

  private final String id;
  private final GitUser user;
  private final Repository repo;

  public Workspace(@Nonnull String id, @Nonnull GitUser user, @Nonnull Repository repo) {
    this.id = id;
    this.user = user;
    this.repo = repo;
  }

  @Nonnull
  public Object getResource(@Nonnull ResourceRequest request) throws IOException {
    switch(request.getType()) {
      case "branches":
        return getBranches();
      default:
        throw new UnsupportedOperationException(request.getType());
    }
  }

  @Nonnull
  public Set<String> getBranches() throws IOException {
    return BranchUtils.getBranches(repo).keySet();
  }

}
