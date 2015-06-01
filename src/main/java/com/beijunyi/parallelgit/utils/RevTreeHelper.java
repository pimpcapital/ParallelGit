package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevTree;

public class RevTreeHelper {

  /**
   * Gets the root {@code RevTree} of the given commit.
   *
   * @param   reader
   *          an object reader
   * @param   commitId
   *          the id of a commit
   * @return  root {@code RevTree} of the given commit.
   */
  @Nonnull
  public static RevTree getRootTree(@Nonnull ObjectReader reader, @Nonnull AnyObjectId commitId) throws IOException {
    return CommitHelper.getCommit(reader, commitId).getTree();
  }

  /**
   * Gets the root {@code RevTree} of the given commit.
   *
   * @param   repo
   *          a git repo
   * @param   commitId
   *          the id of a commit
   * @return  root {@code RevTree} of the given commit.
   */
  @Nonnull
  public static RevTree getRootTree(@Nonnull Repository repo, @Nonnull AnyObjectId commitId) throws IOException {
    return CommitHelper.getCommit(repo, commitId).getTree();
  }

}
