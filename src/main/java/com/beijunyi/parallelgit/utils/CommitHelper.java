package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public final class CommitHelper {

  @Nonnull
  public static RevCommit getCommit(@Nonnull ObjectReader reader, @Nonnull AnyObjectId commitId) throws IOException {
      RevWalk revWalk = new RevWalk(reader);
      RevCommit commit = revWalk.parseCommit(commitId);
      revWalk.release();
      return commit;
    }

  @Nonnull
  public static RevCommit getCommit(@Nonnull Repository repo, @Nonnull AnyObjectId commitId) throws IOException {
    ObjectReader reader = repo.newObjectReader();
    RevCommit commit = getCommit(reader, commitId);
    reader.release();
    return commit;
  }

  @Nullable
  public static RevCommit getCommit(@Nonnull Repository repo, @Nonnull String revision) throws IOException {
    ObjectId commitId = repo.resolve(revision);
    if(commitId == null)
      return null;
    return getCommit(repo, commitId);
  }

  @Nonnull
  public static RevWalk iterateCommits(@Nonnull RevWalk revWalk, @Nonnull RevCommit start) throws IOException {
    revWalk.markStart(start);
    return revWalk;
  }

  @Nonnull
  public static RevWalk iterateCommits(@Nonnull ObjectReader reader, @Nonnull RevCommit start) throws IOException {
    return iterateCommits(new RevWalk(reader), start);
  }

  @Nonnull
  public static RevWalk iterateCommits(@Nonnull Repository repo, @Nonnull RevCommit start) throws IOException {
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
  public static ObjectId createCommit(@Nonnull ObjectInserter inserter, @Nonnull ObjectId treeId, @Nonnull PersonIdent author, @Nonnull PersonIdent committer, @Nonnull String message, @Nullable List<ObjectId> parents) throws IOException {
    CommitBuilder commit = new CommitBuilder();
    commit.setCommitter(committer);
    commit.setAuthor(author);
    commit.setMessage(message);
    commit.setTreeId(treeId);
    if(parents != null)
      commit.setParentIds(parents);

    return inserter.insert(commit);
  }

  @Nonnull
  public static ObjectId createCommit(@Nonnull ObjectInserter inserter, @Nonnull ObjectId treeId, @Nonnull PersonIdent author, @Nonnull PersonIdent committer, @Nonnull String message, @Nullable ObjectId parent) throws IOException {
    List<ObjectId> parents = parent != null ? Collections.singletonList(parent) : null;
    return createCommit(inserter, treeId, author, committer, message, parents);
  }

  @Nonnull
  public static ObjectId createCommit(@Nonnull ObjectInserter inserter, @Nonnull DirCache cache, @Nonnull PersonIdent author, @Nonnull PersonIdent committer, @Nonnull String message, @Nullable List<ObjectId> parents) throws IOException {
    ObjectId treeId = cache.writeTree(inserter);
    return createCommit(inserter, treeId, author, committer, message, parents);
  }

  @Nonnull
  public static ObjectId createCommit(@Nonnull ObjectInserter inserter, @Nonnull DirCache cache, @Nonnull PersonIdent author, @Nonnull PersonIdent committer, @Nonnull String message, @Nullable ObjectId parent) throws IOException {
    List<ObjectId> parents = parent != null ? Collections.singletonList(parent) : null;
    return createCommit(inserter, cache, author, committer, message, parents);
  }

  @Nonnull
  public static ObjectId createCommit(@Nonnull Repository repo, @Nonnull DirCache cache, @Nonnull PersonIdent author, @Nonnull PersonIdent committer, @Nonnull String message, @Nullable ObjectId parent) throws IOException {
    ObjectInserter inserter = repo.newObjectInserter();
    try {
      ObjectId resultCommitId = createCommit(inserter, cache, author, committer, message, parent);
      inserter.flush();
      return resultCommitId;
    } finally {
      inserter.release();
    }
  }

  @Nonnull
  public static ObjectId createCommit(@Nonnull Repository repo, @Nonnull DirCache cache, @Nonnull PersonIdent author, @Nonnull String message, @Nullable ObjectId parent) throws IOException {
    return createCommit(repo, cache, author, author, message, parent);
  }

}
