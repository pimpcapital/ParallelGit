package com.beijunyi.parallelgit.util;

import java.io.IOException;
import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;

public final class RevTreeHelper {

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

  /**
   * Gets the root {@code RevTree} of the given commit.
   *
   * @param   repo
   *          a git repo
   * @param   commitIdStr
   *          the id of a commit
   * @return  root {@code RevTree} of the given commit.
   */
  @Nonnull
  public static RevTree getRootTree(@Nonnull Repository repo, @Nonnull String commitIdStr) throws IOException {
    return CommitHelper.getCommit(repo, repo.resolve(commitIdStr)).getTree();
  }

}
