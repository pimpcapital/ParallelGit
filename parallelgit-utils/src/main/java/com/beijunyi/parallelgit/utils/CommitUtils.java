package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.exceptions.NoSuchCommitException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevWalkUtils;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

public final class CommitUtils {

  @Nonnull
  public static RevCommit getCommit(AnyObjectId commitId, ObjectReader reader) throws IOException {
    try(RevWalk revWalk = new RevWalk(reader)) {
      return revWalk.parseCommit(commitId);
    }
  }

  @Nonnull
  public static RevCommit getCommit(AnyObjectId commitId, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return getCommit(commitId, reader);
    }
  }

  @Nonnull
  public static RevCommit getCommit(Ref ref, ObjectReader reader) throws IOException {
    return getCommit(ref.getObjectId(), reader);
  }

  @Nonnull
  public static RevCommit getCommit(Ref ref, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return getCommit(ref, reader);
    }
  }

  @Nonnull
  public static RevCommit getCommit(String id, Repository repo) throws IOException {
    AnyObjectId commitId = repo.resolve(id);
    if(commitId == null)
      throw new NoSuchCommitException(id);
    return getCommit(commitId, repo);
  }

  public static boolean commitExists(String id, Repository repo) throws IOException {
    AnyObjectId obj = repo.resolve(id);
    if(obj == null)
      return false;
    try(RevWalk rw = new RevWalk(repo)) {
      return rw.parseAny(obj).getType() == Constants.OBJ_COMMIT;
    }
  }

  @Nonnull
  public static List<RevCommit> getCommitHistory(AnyObjectId start, int skip, int limit, ObjectReader reader) throws IOException {
    return getCommitHistory(start, skip, limit, null, reader);
  }

  @Nonnull
  public static List<RevCommit> getCommitHistory(AnyObjectId start, ObjectReader reader) throws IOException {
    return getCommitHistory(start, 0, Integer.MAX_VALUE, reader);
  }

  @Nonnull
  public static List<RevCommit> getCommitHistory(AnyObjectId start, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return getCommitHistory(start, reader);
    }
  }

  @Nonnull
  public static List<RevCommit> getFileRevisions(String file, AnyObjectId start, int skip, int limit, ObjectReader reader) throws IOException {
    file = TreeUtils.normalizeTreePath(file);
    TreeFilter filter = AndTreeFilter.create(PathFilterGroup.createFromStrings(file), TreeFilter.ANY_DIFF);
    return getCommitHistory(start, skip, limit, filter, reader);
  }

  @Nonnull
  public static List<RevCommit> getFileRevisions(String file, AnyObjectId start, ObjectReader reader) throws IOException {
    return getFileRevisions(file, start, 0, Integer.MAX_VALUE, reader);
  }

  @Nonnull
  public static List<RevCommit> getFileRevisions(String file, AnyObjectId start, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return getFileRevisions(file, start, reader);
    }
  }

  @Nonnull
  public static RevCommit getFileLastChangeCommit(String file, AnyObjectId start, ObjectReader reader) throws IOException {
    List<RevCommit> commits = getFileRevisions(file, start, 0, 1, reader);
    if(commits.isEmpty())
      throw new NoSuchFileException(file);
    return commits.get(0);
  }

  @Nonnull
  public static RevCommit getFileLastChangeCommit(String file, AnyObjectId start, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return getFileLastChangeCommit(file, start, reader);
    }
  }

  public static boolean isMergedInto(AnyObjectId base, AnyObjectId target, ObjectReader reader) throws IOException {
    try(RevWalk rw = new RevWalk(reader)) {
      return rw.isMergedInto(rw.lookupCommit(base), rw.lookupCommit(target));
    }
  }

  public static boolean isMergedInto(AnyObjectId base, AnyObjectId target, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return isMergedInto(base, target, reader);
    }
  }

  @Nonnull
  public static List<RevCommit> findSquashableCommits(RevCommit start, @Nullable RevCommit end, ObjectReader reader) throws IOException {
    try(RevWalk rw = new RevWalk(reader)) {
      return RevWalkUtils.find(rw, start, end);
    }
  }

  @Nonnull
  public static List<RevCommit> findSquashableCommits(RevCommit start, @Nullable RevCommit end, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return findSquashableCommits(start, end, reader);
    }
  }

  @Nonnull
  public static RevCommit createCommit(String message, AnyObjectId treeId, PersonIdent author, PersonIdent committer, List<? extends AnyObjectId> parents, Repository repo) throws IOException {
    try(ObjectInserter inserter = repo.newObjectInserter()) {
      AnyObjectId commitId = insertCommit(message, treeId, author, committer, parents, inserter);
      inserter.flush();
      return CommitUtils.getCommit(commitId, repo);
    }
  }

  @Nonnull
  public static RevCommit createCommit(String message, AnyObjectId treeId, PersonIdent committer, @Nullable AnyObjectId parent, Repository repo) throws IOException {
    return createCommit(message, treeId, committer, committer, toParentList(parent), repo);
  }

  @Nonnull
  public static RevCommit createCommit(String message, AnyObjectId treeId, @Nullable AnyObjectId parent, Repository repo) throws IOException {
    return createCommit(message, treeId, new PersonIdent(repo), parent, repo);
  }

  @Nonnull
  public static RevCommit createCommit(String message, DirCache cache, PersonIdent author, PersonIdent committer, List<? extends AnyObjectId> parents, Repository repo) throws IOException {
    try(ObjectInserter inserter = repo.newObjectInserter()) {
      AnyObjectId commitId = insertCommit(message, cache.writeTree(inserter), author, committer, parents, inserter);
      inserter.flush();
      return CommitUtils.getCommit(commitId, repo);
    }
  }

  @Nonnull
  public static RevCommit createCommit(String message, DirCache cache, PersonIdent committer, @Nullable AnyObjectId parent, Repository repo) throws IOException {
    return createCommit(message, cache, committer, committer, toParentList(parent), repo);
  }

  @Nonnull
  public static RevCommit createCommit(String message, DirCache cache, @Nullable AnyObjectId parent, Repository repo) throws IOException {
    return createCommit(message, cache, new PersonIdent(repo), parent, repo);
  }

  @Nonnull
  private static ObjectId insertCommit(String message, AnyObjectId treeId, PersonIdent author, PersonIdent committer, List<? extends AnyObjectId> parents, ObjectInserter inserter) throws IOException {
    CommitBuilder builder = new CommitBuilder();
    builder.setCommitter(committer);
    builder.setAuthor(author);
    builder.setMessage(message);
    builder.setTreeId(treeId);
    builder.setParentIds(parents);
    return inserter.insert(builder);
  }

  @Nonnull
  private static List<? extends AnyObjectId> toParentList(@Nullable AnyObjectId parent) {
    return parent != null ? Collections.singletonList(parent) : Collections.<AnyObjectId>emptyList();
  }

  @Nonnull
  private static List<RevCommit> getCommitHistory(AnyObjectId start, int skip, int limit, @Nullable TreeFilter filter, ObjectReader reader) throws IOException {
    List<RevCommit> commits;
    try(RevWalk rw = new RevWalk(reader)) {
      rw.markStart(CommitUtils.getCommit(start, reader));
      if(filter != null)
        rw.setTreeFilter(filter);
      commits = toCommitList(rw, skip, limit);
    }
    return commits;
  }

  @Nonnull
  private static List<RevCommit> toCommitList(RevWalk rw, int skip, int limit) {
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
