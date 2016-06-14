package com.beijunyi.parallelgit.utils;

import java.io.IOException;
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

import static com.beijunyi.parallelgit.utils.TreeUtils.normalizeNodePath;
import static java.util.Collections.unmodifiableList;
import static org.eclipse.jgit.treewalk.filter.TreeFilter.ANY_DIFF;

public final class CommitUtils {

  @Nonnull
  public static String getDefaultCommitName(RevCommit commit) {
    return commit.getId().abbreviate(7).name() + " " + commit.getShortMessage();
  }

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
    ObjectId commitId = repo.resolve(id);
    if(commitId == null)
      throw new NoSuchCommitException(id);
    return getCommit(commitId, repo);
  }

  public static boolean exists(String name, Repository repo) throws IOException {
    ObjectId obj = repo.resolve(name);
    if(obj == null)
      return false;
    try(RevWalk rw = new RevWalk(repo)) {
      return rw.lookupCommit(obj) != null;
    }
  }

  @Nonnull
  public static List<RevCommit> getHistory(AnyObjectId start, int skip, int limit, ObjectReader reader) throws IOException {
    return getHistory(start, skip, limit, null, reader);
  }

  @Nonnull
  public static List<RevCommit> getHistory(AnyObjectId start, ObjectReader reader) throws IOException {
    return getHistory(start, 0, Integer.MAX_VALUE, reader);
  }

  @Nonnull
  public static List<RevCommit> getHistory(AnyObjectId start, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return getHistory(start, reader);
    }
  }

  @Nonnull
  public static List<RevCommit> getFileRevisions(String path, AnyObjectId start, int skip, int limit, ObjectReader reader) throws IOException {
    path = normalizeNodePath(path);
    TreeFilter filter = AndTreeFilter.create(PathFilterGroup.createFromStrings(path), ANY_DIFF);
    return getHistory(start, skip, limit, filter, reader);
  }

  @Nonnull
  public static List<RevCommit> getFileRevisions(String path, AnyObjectId start, ObjectReader reader) throws IOException {
    return getFileRevisions(path, start, 0, Integer.MAX_VALUE, reader);
  }

  @Nonnull
  public static List<RevCommit> getFileRevisions(String file, AnyObjectId start, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return getFileRevisions(file, start, reader);
    }
  }

  @Nonnull
  public static List<RevCommit> getFileRevisions(String file, String start, Repository repo) throws IOException {
    ObjectId id = repo.resolve(start);
    return id != null ? getFileRevisions(file, id, repo) : Collections.<RevCommit>emptyList();
  }

  @Nullable
  public static RevCommit getLatestFileRevision(String path, AnyObjectId start, ObjectReader reader) throws IOException {
    List<RevCommit> commits = getFileRevisions(path, start, 0, 1, reader);
    if(commits.isEmpty()) return null;
    return commits.get(0);
  }

  @Nullable
  public static RevCommit getLatestFileRevision(String path, AnyObjectId start, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return getLatestFileRevision(path, start, reader);
    }
  }

  @Nullable
  public static RevCommit getLatestFileRevision(String path, String start, Repository repo) throws IOException {
    ObjectId id = repo.resolve(start);
    return id != null ? getLatestFileRevision(path, id, repo) : null;
  }

  public static boolean isMergedInto(AnyObjectId sourceHead, AnyObjectId masterHead, ObjectReader reader) throws IOException {
    try(RevWalk rw = new RevWalk(reader)) {
      return rw.isMergedInto(rw.lookupCommit(sourceHead), rw.lookupCommit(masterHead));
    }
  }

  public static boolean isMergedInto(AnyObjectId sourceHead, AnyObjectId masterHead, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return isMergedInto(sourceHead, masterHead, reader);
    }
  }

  public static boolean isMergedInto(String source, String master, Repository repo) throws IOException {
    return isMergedInto(repo.resolve(source), repo.resolve(master), repo);
  }

  @Nonnull
  public static List<RevCommit> listUnmergedCommits(AnyObjectId sourceHead, AnyObjectId masterHead, ObjectReader reader) throws IOException {
    try(RevWalk rw = new RevWalk(reader)) {
      return unmodifiableList(RevWalkUtils.find(rw, rw.lookupCommit(sourceHead), rw.lookupCommit(masterHead)));
    }
  }

  @Nonnull
  public static List<RevCommit> listUnmergedCommits(AnyObjectId sourceHead, AnyObjectId masterHead, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return listUnmergedCommits(sourceHead, masterHead, reader);
    }
  }

  @Nonnull
  public static List<RevCommit> listUnmergedCommits(String source, String master, Repository repo) throws IOException {
    return listUnmergedCommits(repo.resolve(source), repo.resolve(master), repo);
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
  private static List<RevCommit> getHistory(AnyObjectId start, int skip, int limit, @Nullable TreeFilter filter, ObjectReader reader) throws IOException {
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
    return unmodifiableList(commits);
  }

}
