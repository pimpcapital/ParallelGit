package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;

public final class RevTreeUtils {

  @Nonnull
  public static RevTree getRootTree(@Nonnull ObjectReader reader, @Nonnull AnyObjectId commitId) throws IOException {
    return CommitUtils.getCommit(commitId, reader).getTree();
  }

  @Nonnull
  public static RevTree getRootTree(@Nonnull Repository repo, @Nonnull AnyObjectId commitId) throws IOException {
    return CommitUtils.getCommit(commitId, repo).getTree();
  }

  @Nonnull
  public static RevTree getRootTree(@Nonnull Repository repo, @Nonnull String commitIdStr) throws IOException {
    return CommitUtils.getCommit(repo.resolve(commitIdStr), repo).getTree();
  }

}
