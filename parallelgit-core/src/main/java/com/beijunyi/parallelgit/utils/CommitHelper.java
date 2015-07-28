package com.beijunyi.parallelgit.utils;

import java.io.IOException;
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

  @Nonnull
  public static RevCommit createCommit(@Nonnull Repository repo, @Nonnull AnyObjectId treeId, @Nonnull PersonIdent author, @Nonnull PersonIdent committer, @Nullable String message, @Nonnull List<AnyObjectId> parents) throws IOException {
    CommitBuilder commit = new CommitBuilder();
    commit.setCommitter(committer);
    commit.setAuthor(author);
    commit.setMessage(message);
    commit.setTreeId(treeId);
    commit.setParentIds(parents);

    ObjectInserter inserter = repo.newObjectInserter();
    try {
      AnyObjectId commitId = inserter.insert(commit);
      inserter.flush();
      return getCommit(repo, commitId);
    } finally {
      inserter.release();
    }
  }

  @Nonnull
  public static RevCommit createCommit(@Nonnull Repository repo, @Nonnull DirCache cache, @Nonnull PersonIdent author, @Nonnull PersonIdent committer, @Nullable String message, @Nonnull List<AnyObjectId> parents) throws IOException {
    return createCommit(repo, DirCacheHelper.writeTree(repo, cache), author, committer, message, parents);
  }

}
