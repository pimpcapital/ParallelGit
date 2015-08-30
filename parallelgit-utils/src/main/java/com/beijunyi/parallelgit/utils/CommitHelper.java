package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.StringUtils;

public final class CommitHelper {

  @Nonnull
  public static String convertToShortMessage(@Nonnull String fullMessage) {
    if(fullMessage.contains("\n"))
      return StringUtils.replaceLineBreaksWithSpace(fullMessage);
    return fullMessage;
  }

  @Nonnull
  public static RevCommit getCommit(@Nonnull ObjectReader reader, @Nonnull AnyObjectId commitId) throws IOException {
    try(RevWalk revWalk = new RevWalk(reader)) {
      return revWalk.parseCommit(commitId);
    }
  }

  @Nonnull
  public static RevCommit getCommit(@Nonnull Repository repo, @Nonnull AnyObjectId commitId) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return getCommit(reader, commitId);
    }
  }

  @Nullable
  public static RevCommit getCommit(@Nonnull Repository repo, @Nonnull String revision) throws IOException {
    AnyObjectId commitId = repo.resolve(revision);
    return commitId != null ? getCommit(repo, commitId) : null;
  }

  @Nonnull
  public static List<RevCommit> getCommitHistory(@Nonnull ObjectReader reader, @Nonnull RevCommit start) throws IOException {
    try(RevWalk rw = new RevWalk(reader)) {
      rw.markStart(start);
      List<RevCommit> commits = new ArrayList<>();
      for(RevCommit commit : rw)
        commits.add(commit);
      return commits;
    }
  }

  @Nonnull
  public static List<RevCommit> getCommitHistory(@Nonnull Repository repo, @Nonnull RevCommit start) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return getCommitHistory(reader, start);
    }
  }

  @Nonnull
  public static AnyObjectId createCommit(@Nonnull ObjectInserter inserter, @Nonnull AnyObjectId treeId, @Nonnull PersonIdent author, @Nonnull PersonIdent committer, @Nullable String message, @Nonnull List<AnyObjectId> parents) throws IOException {
    CommitBuilder builder = new CommitBuilder();
    builder.setCommitter(committer);
    builder.setAuthor(author);
    builder.setMessage(message);
    builder.setTreeId(treeId);
    builder.setParentIds(parents);
    return inserter.insert(builder);
  }

  @Nonnull
  public static AnyObjectId createCommit(@Nonnull Repository repo, @Nonnull AnyObjectId treeId, @Nonnull PersonIdent author, @Nonnull PersonIdent committer, @Nullable String message, @Nonnull List<AnyObjectId> parents) throws IOException {
    try(ObjectInserter inserter = repo.newObjectInserter()) {
      AnyObjectId commitId = createCommit(inserter, treeId, author, committer, message, parents);
      inserter.flush();
      return commitId;
    }
  }

  @Nonnull
  public static AnyObjectId createCommit(@Nonnull ObjectInserter inserter, @Nonnull DirCache cache, @Nonnull PersonIdent author, @Nonnull PersonIdent committer, @Nullable String message, @Nonnull List<AnyObjectId> parents) throws IOException {
    return createCommit(inserter, cache.writeTree(inserter), author, committer, message, parents);
  }

  @Nonnull
  public static AnyObjectId createCommit(@Nonnull Repository repo, @Nonnull DirCache cache, @Nonnull PersonIdent author, @Nonnull PersonIdent committer, @Nullable String message, @Nonnull List<AnyObjectId> parents) throws IOException {
    try(ObjectInserter inserter = repo.newObjectInserter()) {
      AnyObjectId commitId = createCommit(inserter, cache, author, committer, message, parents);
      inserter.flush();
      return commitId;
    }
  }

  @Nonnull
  public static AnyObjectId createCommit(@Nonnull Repository repo, @Nonnull DirCache cache, @Nonnull PersonIdent committer, @Nullable String message, @Nonnull List<AnyObjectId> parents) throws IOException {
    return createCommit(repo, cache, committer, committer, message, parents);
  }

  @Nonnull
  public static AnyObjectId createCommit(@Nonnull Repository repo, @Nonnull DirCache cache, @Nonnull PersonIdent committer, @Nullable String message, @Nullable AnyObjectId parent) throws IOException {
    List<AnyObjectId> parents = parent != null ? Collections.singletonList(parent) : Collections.<AnyObjectId>emptyList();
    return createCommit(repo, cache, committer, message, parents);
  }

  @Nonnull
  public static AnyObjectId createCommit(@Nonnull Repository repo, @Nonnull DirCache cache, @Nonnull PersonIdent committer, @Nullable String message) throws IOException {
    return createCommit(repo, cache, committer, message, (AnyObjectId) null);
  }

}
