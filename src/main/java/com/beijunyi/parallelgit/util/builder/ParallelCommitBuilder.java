package com.beijunyi.parallelgit.util.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.util.BranchHelper;
import com.beijunyi.parallelgit.util.CommitHelper;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;

public final class ParallelCommitBuilder extends CacheBasedBuilder<ParallelCommitBuilder, ObjectId> {
  private String branch;
  private boolean orphan;
  private boolean amend;
  private boolean allowEmptyCommit;
  private AnyObjectId treeId;
  private DirCache cache;
  private PersonIdent author;
  private String authorName;
  private String authorEmail;
  private PersonIdent committer;
  private String committerName;
  private String committerEmail;
  private RevCommit head;
  private String message;
  private List<AnyObjectId> parents;

  private ParallelCommitBuilder(@Nonnull Repository repository) {
    super(repository);
  }

  @Nonnull
  @Override
  protected ParallelCommitBuilder self() {
    return this;
  }

  @Nonnull
  public ParallelCommitBuilder branch(@Nonnull String branch) {
    this.branch = branch;
    return this;
  }

  @Nonnull
  public ParallelCommitBuilder orphan(boolean orphan) {
    this.orphan = orphan;
    return this;
  }

  @Nonnull
  public ParallelCommitBuilder amend(boolean amend) {
    this.amend = amend;
    return this;
  }

  @Nonnull
  public ParallelCommitBuilder allowEmptyCommit(boolean allowEmptyCommit) {
    this.allowEmptyCommit = allowEmptyCommit;
    return this;
  }

  @Nonnull
  public ParallelCommitBuilder withTree(@Nonnull AnyObjectId treeId) {
    this.treeId = treeId;
    return this;
  }

  @Nonnull
  public ParallelCommitBuilder fromCache(@Nonnull DirCache cache) {
    this.cache = cache;
    return this;
  }

  @Nonnull
  public ParallelCommitBuilder author(@Nonnull PersonIdent author) {
    this.author = author;
    return this;
  }

  @Nonnull
  public ParallelCommitBuilder author(@Nonnull String authorName, @Nonnull String authorEmail) {
    this.authorName = authorName;
    this.authorEmail = authorEmail;
    return this;
  }

  @Nonnull
  public ParallelCommitBuilder committer(@Nonnull PersonIdent committer) {
    this.committer = committer;
    return this;
  }

  @Nonnull
  public ParallelCommitBuilder committer(@Nonnull String committerName, @Nonnull String committerEmail) {
    this.committerName = committerName;
    this.committerEmail = committerEmail;
    return this;
  }

  @Nonnull
  public ParallelCommitBuilder message(@Nonnull String message) {
    this.message = message;
    return this;
  }

  @Nonnull
  public ParallelCommitBuilder parents(@Nonnull List<AnyObjectId> parents) {
    this.parents = parents;
    return this;
  }

  @Nonnull
  public ParallelCommitBuilder parents(@Nonnull AnyObjectId... parents) {
    return parents(Arrays.asList(parents));
  }

  private void prepareCache() throws IOException {
    if(cache == null)
      cache = buildCache();
  }

  private void prepareTree(@Nonnull ObjectInserter inserter) throws IOException {
    if(treeId == null) {
      prepareCache();
      treeId = cache.writeTree(inserter);
    }
  }

  private void prepareAuthor() {
    if(author == null) {
      if(authorName != null && authorEmail != null)
        author = new PersonIdent(authorName, authorEmail);
      else
        author = new PersonIdent(repository);
    }
  }

  private void prepareCommitter() {
    if(committer == null) {
      if(committerName != null && committerEmail != null)
        committer = new PersonIdent(committerName, committerEmail);
      else
        committer = author;
    }
  }

  private void prepareMessage() {
    if(message == null && amend && head != null)
      message = head.getFullMessage();
  }

  private void prepareHead() throws IOException {
    assert repository != null;
    head = branch != null ? CommitHelper.getCommit(repository, branch) : null;
  }

  private void prepareParents() throws IOException {
    if(parents != null)
      return;
    parents = new ArrayList<>();
    if(orphan)
      return;
    if(branch != null) {
      assert repository != null;
      if(head != null) {
        if(amend)
          parents.addAll(Arrays.asList(head.getParents()));
        else
          parents.add(head);
      }
    }
  }

  private boolean isDifferentTree() throws IOException {
    if(allowEmptyCommit || parents.isEmpty())
      return true;
    assert repository != null;
    return !treeId.equals(CommitHelper.getCommit(repository, parents.get(0)).getTree());
  }

  private void updateBranchRef(@Nonnull AnyObjectId newCommitId) throws IOException {
    if(branch != null) {
      assert repository != null;
      RevCommit newCommit = CommitHelper.getCommit(repository, newCommitId);
      if(head == null)
        BranchHelper.initBranchHead(repository, branch, newCommit, newCommit.getShortMessage());
      else if(amend)
        BranchHelper.amendBranchHead(repository, branch, newCommit, newCommit.getShortMessage());
      else
        BranchHelper.commitBranchHead(repository, branch, newCommit, newCommit.getShortMessage());
    }
  }

  @Nullable
  @Override
  public ObjectId doBuild() throws IOException {
    assert repository != null;
    ObjectInserter inserter = repository.newObjectInserter();
    try {
      prepareTree(inserter);
      prepareAuthor();
      prepareCommitter();
      prepareMessage();
      prepareHead();
      prepareParents();
      if(!isDifferentTree())
        return null;
      ObjectId commit = CommitHelper.createCommit(inserter, treeId, author, committer, message, parents);
      inserter.flush();
      updateBranchRef(commit);
      return commit;
    } finally {
      inserter.release();
    }
  }

  @Nonnull
  public static ParallelCommitBuilder prepare(@Nonnull Repository repository) {
    return new ParallelCommitBuilder(repository);
  }

}