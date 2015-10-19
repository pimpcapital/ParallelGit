package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

public final class CommitUtils {

  @Nonnull
  public static RevCommit getCommit(@Nonnull AnyObjectId commitId, @Nonnull ObjectReader reader) throws IOException {
    try(RevWalk revWalk = new RevWalk(reader)) {
      return revWalk.parseCommit(commitId);
    }
  }

  @Nonnull
  public static RevCommit getCommit(@Nonnull AnyObjectId commitId, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return getCommit(commitId, reader);
    }
  }

  @Nonnull
  public static RevCommit getCommit(@Nonnull Ref ref, @Nonnull ObjectReader reader) throws IOException {
    return getCommit(ref.getObjectId(), reader);
  }

  @Nonnull
  public static RevCommit getCommit(@Nonnull Ref ref, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return getCommit(ref, reader);
    }
  }

  @Nullable
  public static RevCommit getCommit(@Nonnull String revision, @Nonnull Repository repo) throws IOException {
    AnyObjectId commitId = repo.resolve(revision);
    return commitId != null ? getCommit(commitId, repo) : null;
  }

  @Nonnull
  public static List<RevCommit> getCommitHistory(@Nonnull RevCommit start, int skip, int limit, @Nonnull ObjectReader reader) throws IOException {
    return getCommitHistory(start, skip, limit, null, reader);
  }

  @Nonnull
  public static List<RevCommit> getCommitHistory(@Nonnull RevCommit start, @Nonnull ObjectReader reader) throws IOException {
    return getCommitHistory(start, 0, Integer.MAX_VALUE, reader);
  }

  @Nonnull
  public static List<RevCommit> getCommitHistory(@Nonnull RevCommit start, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return getCommitHistory(start, reader);
    }
  }

  @Nonnull
  public static List<RevCommit> getFileCommitHistory(@Nonnull String file, @Nonnull RevCommit start, int skip, int limit, @Nonnull ObjectReader reader) throws IOException {
    file = TreeUtils.normalizeTreePath(file);
    TreeFilter filter = AndTreeFilter.create(PathFilterGroup.createFromStrings(file), TreeFilter.ANY_DIFF);
    return getCommitHistory(start, skip, limit, filter, reader);
  }

  @Nonnull
  public static List<RevCommit> getFileCommitHistory(@Nonnull String file, @Nonnull RevCommit start, @Nonnull ObjectReader reader) throws IOException {
    return getFileCommitHistory(file, start, 0, Integer.MAX_VALUE, reader);
  }

  @Nonnull
  public static List<RevCommit> getFileCommitHistory(@Nonnull String file, @Nonnull RevCommit start, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return getFileCommitHistory(file, start, reader);
    }
  }

  @Nonnull
  public static RevCommit getFileLastChangeCommit(@Nonnull String file, @Nonnull RevCommit start, @Nonnull ObjectReader reader) throws IOException {
    List<RevCommit> commits = getFileCommitHistory(file, start, 0, 1, reader);
    if(commits.isEmpty())
      throw new NoSuchFileException(file);
    return commits.get(0);
  }

  @Nonnull
  public static RevCommit getFileLastChangeCommit(@Nonnull String file, @Nonnull RevCommit start, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return getFileLastChangeCommit(file, start, reader);
    }
  }

  @Nonnull
  public static RevCommit createCommit(@Nonnull String message, @Nonnull AnyObjectId treeId, @Nonnull PersonIdent author, @Nonnull PersonIdent committer, @Nonnull List<AnyObjectId> parents, @Nonnull Repository repo) throws IOException {
    try(ObjectInserter inserter = repo.newObjectInserter()) {
      AnyObjectId commitId = insertCommit(message, treeId, author, committer, parents, inserter);
      inserter.flush();
      return CommitUtils.getCommit(commitId, repo);
    }
  }

  @Nonnull
  public static RevCommit createCommit(@Nonnull String message, @Nonnull AnyObjectId treeId, @Nonnull PersonIdent committer, @Nullable AnyObjectId parent, @Nonnull Repository repo) throws IOException {
    return createCommit(message, treeId, committer, committer, toParentList(parent), repo);
  }

  @Nonnull
  public static RevCommit createCommit(@Nonnull String message, @Nonnull AnyObjectId treeId, @Nullable AnyObjectId parent, @Nonnull Repository repo) throws IOException {
    return createCommit(message, treeId, new PersonIdent(repo), parent, repo);
  }

  @Nonnull
  public static RevCommit createCommit(@Nonnull String message, @Nonnull DirCache cache, @Nonnull PersonIdent author, @Nonnull PersonIdent committer, @Nonnull List<AnyObjectId> parents, @Nonnull Repository repo) throws IOException {
    try(ObjectInserter inserter = repo.newObjectInserter()) {
      AnyObjectId commitId = insertCommit(message, cache.writeTree(inserter), author, committer, parents, inserter);
      inserter.flush();
      return CommitUtils.getCommit(commitId, repo);
    }
  }

  @Nonnull
  public static RevCommit createCommit(@Nonnull String message, @Nonnull DirCache cache, @Nonnull PersonIdent committer, @Nullable AnyObjectId parent, @Nonnull Repository repo) throws IOException {
    return createCommit(message, cache, committer, committer, toParentList(parent), repo);
  }

  @Nonnull
  public static RevCommit createCommit(@Nonnull String message, @Nonnull DirCache cache, @Nullable AnyObjectId parent, @Nonnull Repository repo) throws IOException {
    return createCommit(message, cache, new PersonIdent(repo), parent, repo);
  }

  @Nonnull
  private static AnyObjectId insertCommit(@Nonnull String message, @Nonnull AnyObjectId treeId, @Nonnull PersonIdent author, @Nonnull PersonIdent committer, @Nonnull List<AnyObjectId> parents, @Nonnull ObjectInserter inserter) throws IOException {
    CommitBuilder builder = new CommitBuilder();
    builder.setCommitter(committer);
    builder.setAuthor(author);
    builder.setMessage(message);
    builder.setTreeId(treeId);
    builder.setParentIds(parents);
    return inserter.insert(builder);
  }

  @Nonnull
  private static List<AnyObjectId> toParentList(@Nullable AnyObjectId parent) {
    return parent != null ? Collections.singletonList(parent) : Collections.<AnyObjectId>emptyList();
  }

  @Nonnull
  private static List<RevCommit> getCommitHistory(@Nonnull RevCommit start, int skip, int limit, @Nullable TreeFilter filter, @Nonnull ObjectReader reader) throws IOException {
    List<RevCommit> commits;
    try(RevWalk rw = new RevWalk(reader)) {
      rw.markStart(start);
      if(filter != null)
        rw.setTreeFilter(filter);
      commits = toCommitList(rw, skip, limit);
    }
    return commits;
  }

  @Nonnull
  private static List<RevCommit> toCommitList(@Nonnull RevWalk rw, int skip, int limit) {
    List<RevCommit> commits = new ArrayList<>();
    long max = (long) limit + skip;
    long count = 0;
    for(RevCommit commit : rw) {
      if(count >= skip && count < max)
        commits.add(commit);
      if(count++ >= max)
        break;
    }
    return commits;
  }

}
