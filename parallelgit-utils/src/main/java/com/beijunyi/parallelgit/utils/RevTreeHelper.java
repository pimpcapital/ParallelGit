package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;

public final class RevTreeHelper {

  @Nonnull
  public static RevTree getRootTree(@Nonnull ObjectReader reader, @Nonnull AnyObjectId commitId) throws IOException {
    return CommitHelper.getCommit(reader, commitId).getTree();
  }

  @Nonnull
  public static RevTree getRootTree(@Nonnull Repository repo, @Nonnull AnyObjectId commitId) throws IOException {
    return CommitHelper.getCommit(repo, commitId).getTree();
  }

  @Nonnull
  public static RevTree getRootTree(@Nonnull Repository repo, @Nonnull String commitIdStr) throws IOException {
    return CommitHelper.getCommit(repo, repo.resolve(commitIdStr)).getTree();
  }

}
