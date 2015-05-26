package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.ParallelGitException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public final class CommitHelper {

  @Nonnull
  public static RevCommit getCommit(@Nonnull RevWalk revWalk, @Nonnull ObjectId commitId) {
    try {
      return revWalk.parseCommit(commitId);
    } catch(IOException e) {
      throw new ParallelGitException("Could not get the commit for " + commitId, e);
    }
  }

  @Nonnull
  public static RevCommit getCommit(@Nonnull ObjectReader reader, @Nonnull ObjectId commitId) {
      RevWalk revWalk = new RevWalk(reader);
      RevCommit commit = getCommit(revWalk, commitId);
      revWalk.release();
      return commit;
    }

  @Nonnull
  public static RevCommit getCommit(@Nonnull Repository repo, @Nonnull ObjectId commitId) {
    ObjectReader reader = repo.newObjectReader();
    RevCommit commit = getCommit(reader, commitId);
    reader.release();
    return commit;
  }

  @Nullable
  public static RevCommit getCommit(@Nonnull Repository repo, @Nonnull String revision) {
    ObjectId commitId = RepositoryHelper.getRevisionId(repo, revision);
    if(commitId == null)
      return null;
    return getCommit(repo, commitId);
  }

  @Nonnull
  public static RevWalk iterateCommits(@Nonnull RevWalk revWalk, @Nonnull RevCommit start) {
    RevWalkHelper.markStart(revWalk, start);
    return revWalk;
  }

  @Nonnull
  public static RevWalk iterateCommits(@Nonnull ObjectReader reader, @Nonnull RevCommit start) {
    return iterateCommits(new RevWalk(reader), start);
  }

  @Nonnull
  public static RevWalk iterateCommits(@Nonnull Repository repo, @Nonnull RevCommit start) {
    return iterateCommits(repo.newObjectReader(), start);
  }

  /**
   * Constructs a new commit.
   *
   * @param inserter a object inserter
   * @param treeId the root of the commit
   * @param author the author information
   * @param committer the committer information
   * @param message the commit message
   * @param parents the entire list of parents for this commit
   * @return a {@link org.eclipse.jgit.lib.ObjectId} object representing the successful commit.
   */
  @Nonnull
  public static ObjectId createCommit(@Nonnull ObjectInserter inserter, @Nonnull ObjectId treeId, @Nonnull PersonIdent author, @Nonnull PersonIdent committer, @Nonnull String message, @Nullable List<ObjectId> parents) {
    try {
      CommitBuilder commit = new CommitBuilder();
      commit.setCommitter(committer);
      commit.setAuthor(author);
      commit.setMessage(message);
      commit.setTreeId(treeId);
      if(parents != null)
        commit.setParentIds(parents);

      return inserter.insert(commit);
    } catch(IOException e) {
      throw new ParallelGitException("Could not create commit " + message, e);
    }
  }

  @Nonnull
  public static ObjectId createCommit(@Nonnull ObjectInserter inserter, @Nonnull ObjectId treeId, @Nonnull PersonIdent author, @Nonnull PersonIdent committer, @Nonnull String message, @Nullable ObjectId parent) {
    List<ObjectId> parents = parent != null ? Collections.singletonList(parent) : null;
    return createCommit(inserter, treeId, author, committer, message, parents);
  }


    @Nonnull
  public static ObjectId createCommit(@Nonnull ObjectInserter inserter, @Nonnull DirCache cache, @Nonnull PersonIdent author, @Nonnull PersonIdent committer, @Nonnull String message, @Nullable List<ObjectId> parents) {
    ObjectId treeId = DirCacheHelper.writeTree(cache, inserter);
    return createCommit(inserter, treeId, author, committer, message, parents);
  }

  @Nonnull
  public static ObjectId createCommit(@Nonnull ObjectInserter inserter, @Nonnull DirCache cache, @Nonnull PersonIdent author, @Nonnull PersonIdent committer, @Nonnull String message, @Nullable ObjectId parent) {
    List<ObjectId> parents = parent != null ? Collections.singletonList(parent) : null;
    return createCommit(inserter, cache, author, committer, message, parents);
  }

  @Nonnull
  public static ObjectId createCommit(@Nonnull Repository repo, @Nonnull DirCache cache, @Nonnull PersonIdent author, @Nonnull PersonIdent committer, @Nonnull String message, @Nullable ObjectId parent) {
    ObjectInserter inserter = repo.newObjectInserter();
    try {
      ObjectId resultCommitId = createCommit(inserter, cache, author, committer, message, parent);
      RepositoryHelper.flush(inserter);
      return resultCommitId;
    } finally {
      inserter.release();
    }
  }

  @Nonnull
  public static ObjectId createCommit(@Nonnull Repository repo, @Nonnull DirCache cache, @Nonnull PersonIdent author, @Nonnull String message, @Nullable ObjectId parent) {
    return createCommit(repo, cache, author, author, message, parent);
  }

}
