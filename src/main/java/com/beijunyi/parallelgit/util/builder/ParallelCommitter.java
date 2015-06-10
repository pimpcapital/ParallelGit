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

public final class ParallelCommitter extends CacheBasedBuilder<ParallelCommitter, ObjectId> {
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
  private String message;
  private List<AnyObjectId> parents;

  private ParallelCommitter(@Nonnull Repository repository) {
    super(repository);
  }

  @Nonnull
  @Override
  protected ParallelCommitter self() {
    return this;
  }

  @Nonnull
  public ParallelCommitter branch(@Nonnull String branch) {
    this.branch = branch;
    return this;
  }

  public void setOrphan(boolean orphan) {
    this.orphan = orphan;
  }

  public void setAmend(boolean amend) {
    this.amend = amend;
  }

  public void setAllowEmptyCommit(boolean allowEmptyCommit) {
    this.allowEmptyCommit = allowEmptyCommit;
  }

  @Nonnull
  public ParallelCommitter withTree(@Nonnull AnyObjectId treeId) {
    this.treeId = treeId;
    return this;
  }

  @Nonnull
  public ParallelCommitter withCache(@Nonnull DirCache cache) {
    this.cache = cache;
    return this;
  }

  private void checkContent() {
    if(!amend && treeId == null && cache == null && editors.isEmpty())
      throw new IllegalArgumentException("Nothing to commit");
  }

  @Nonnull
  public ParallelCommitter withAuthor(@Nonnull PersonIdent author) {
    this.author = author;
    return this;
  }

  @Nonnull
  public ParallelCommitter withAuthor(@Nonnull String authorName, @Nonnull String authorEmail) {
    this.authorName = authorName;
    this.authorEmail = authorEmail;
    return this;
  }

  private void checkAuthor() {
    if(author == null) {
      if(authorName == null && authorEmail == null)
        throw new IllegalArgumentException("Author must be configured");
      if(authorName == null)
        throw new IllegalArgumentException("Missing author name");
      if(authorEmail == null)
        throw new IllegalArgumentException("Missing author email");
    }
  }

  @Nonnull
  public ParallelCommitter withCommitter(@Nonnull PersonIdent committer) {
    this.committer = committer;
    return this;
  }

  @Nonnull
  public ParallelCommitter withCommitter(@Nonnull String committerName, @Nonnull String committerEmail) {
    this.committerName = committerName;
    this.committerEmail = committerEmail;
    return this;
  }

  private void checkCommitter() {
    if(committerName == null && committerEmail != null)
      throw new IllegalArgumentException("Missing committer name");
    if(committerName != null && committerEmail == null)
      throw new IllegalArgumentException("Missing committer email");
  }

  @Nonnull
  public ParallelCommitter withMessage(@Nonnull String message) {
    this.message = message;
    return this;
  }

  private void ensureMessage() {
    if(amend)
      return;
    if(message == null)
      throw new IllegalArgumentException("Message must be configured");
    if(message.isEmpty())
      throw new IllegalArgumentException("Message must not be empty");
  }

  @Nonnull
  public ParallelCommitter setParents(@Nonnull List<AnyObjectId> parents) {
    this.parents = parents;
    return this;
  }

  @Nonnull
  public ParallelCommitter addParent(@Nonnull AnyObjectId parent) {
    if(parents == null)
      parents = new ArrayList<>();
    parents.add(parent);
    return this;
  }

  @Nonnull
  public ParallelCommitter addParent(@Nonnull AnyObjectId parent, int index) {
    if(parents == null)
      parents = new ArrayList<>();
    parents.add(index, parent);
    return this;
  }

  @Nonnull
  private DirCache prepareCache() throws IOException {
    return cache != null ? cache : (cache = buildCache());
  }

  @Nonnull
  private AnyObjectId prepareTree(@Nonnull ObjectInserter inserter) throws IOException {
    return treeId != null ? treeId : (treeId = prepareCache().writeTree(inserter));
  }

  private boolean isDifferentTree(@Nonnull AnyObjectId treeId, @Nullable RevCommit head) {
    return head == null || !treeId.equals(head.getTree());
  }

  @Nonnull
  private PersonIdent prepareAuthor() {
    return author != null ? author : (author = new PersonIdent(authorName, authorEmail));
  }

  @Nonnull
  private PersonIdent prepareCommitter() {
    if(committer != null)
      return committer;
    if(committerName != null && committerEmail != null)
      return committer = new PersonIdent(committerName, committerEmail);
    return committer = author;
  }

  @Nonnull
  private String prepareMessage(@Nullable RevCommit head) {
    if(message != null)
      return message;
    if(!amend)
      throw new IllegalStateException("Missing message");
    if(head == null)
      throw new IllegalArgumentException("No commit to amend");
    return message = head.getFullMessage();
  }

  @Nonnull
  private List<AnyObjectId> prepareParents(@Nullable RevCommit head) throws IOException {
    if(parents != null)
      return parents;
    parents = new ArrayList<>();
    if(orphan)
      return parents;
    if(branch != null) {
      assert repository != null;
      if(head != null) {
        if(amend)
          parents.addAll(Arrays.asList(head.getParents()));
        else
          parents.add(head);
      }
    }
    return parents;
  }

  private void updateBranchRef(boolean initBranch, @Nonnull AnyObjectId newCommitId) throws IOException {
    assert repository != null;
    RevCommit newCommit = CommitHelper.getCommit(repository, newCommitId);
    if(initBranch)
      BranchHelper.initBranchHead(repository, branch, newCommit, newCommit.getShortMessage());
    else if(amend)
      BranchHelper.amendBranchHead(repository, branch, newCommit, newCommit.getShortMessage());
    else
      BranchHelper.commitBranchHead(repository, branch, newCommit, newCommit.getShortMessage());
  }

  @Nullable
  private ObjectId commit() throws IOException {
    assert repository != null;
    ObjectInserter inserter = repository.newObjectInserter();
    try {
      AnyObjectId commitTree = prepareTree(inserter);
      RevCommit head = CommitHelper.getCommit(repository, branch);
      if(!allowEmptyCommit && !isDifferentTree(commitTree, head))
        return null;
      ObjectId commit = CommitHelper.createCommit(inserter,
                                                   commitTree,
                                                   prepareAuthor(),
                                                   prepareCommitter(),
                                                   prepareMessage(head),
                                                   prepareParents(head));
      inserter.flush();
      updateBranchRef(head == null, commit);
      return commit;
    } finally {
      inserter.release();
    }
  }

  @Nullable
  @Override
  public ObjectId doBuild() throws IOException {
    checkContent();
    checkAuthor();
    checkCommitter();
    ensureMessage();
    return commit();
  }

  @Nonnull
  public static ParallelCommitter prepare(@Nonnull Repository repository) {
    return new ParallelCommitter(repository);
  }

}