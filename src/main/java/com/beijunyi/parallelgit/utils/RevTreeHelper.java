package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;

public class RevTreeHelper {

  /**
   * Gets the {@link RevTree} of the given commit.
   *
   * @param reader an object reader
   * @param commitId a commit id
   * @return the rev tree of the given commit
   */
  @Nonnull
  public static RevTree getRootTree(@Nonnull ObjectReader reader, @Nonnull ObjectId commitId) throws IOException {
    return CommitHelper.getCommit(reader, commitId).getTree();
  }

  /**
   * Gets the {@link RevTree} of the given commit.
   *
   * @param repo a git repository
   * @param commitId a commit id
   * @return the rev tree of the given commit
   */
  @Nonnull
  public static RevTree getRootTree(@Nonnull Repository repo, @Nonnull ObjectId commitId) throws IOException {
    return CommitHelper.getCommit(repo, commitId).getTree();
  }

}
