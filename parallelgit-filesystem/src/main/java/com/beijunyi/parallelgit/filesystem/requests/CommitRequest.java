package com.beijunyi.parallelgit.filesystem.requests;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.utils.BranchUtils;
import com.beijunyi.parallelgit.utils.CommitUtils;
import com.beijunyi.parallelgit.utils.RefUtils;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

public final class CommitRequest extends GitFileSystemRequest<RevCommit> {

  private final String branchRef;
  private final RevCommit commit;
  private PersonIdent author;
  private PersonIdent committer;
  private String message;
  private List<AnyObjectId> parents;
  private boolean amend = false;
  private boolean allowEmpty = false;

  private CommitRequest(@Nonnull GitFileSystem gfs) {
    super(gfs);
    String branch = gfs.getBranch();
    branchRef = branch != null ? RefUtils.ensureBranchRefName(branch) : null;
    commit = gfs.getCommit();
  }

  @Nonnull
  static CommitRequest prepare(@Nonnull GitFileSystem gfs) {
    return new CommitRequest(gfs);
  }

  @Nonnull
  public CommitRequest author(@Nullable PersonIdent author) {
    this.author = author;
    return this;
  }

  @Nonnull
  public CommitRequest committer(@Nullable PersonIdent committer) {
    this.committer = committer;
    return this;
  }

  @Nonnull
  public CommitRequest message(@Nullable String message) {
    this.message = message;
    return this;
  }

  @Nonnull
  public CommitRequest amend(boolean amend) {
    this.amend = amend;
    return this;
  }

  @Nonnull
  public CommitRequest allowEmpty(boolean allowEmpty) {
    this.allowEmpty = allowEmpty;
    return this;
  }

  @Nonnull
  private RevCommit amendedCommit() {
    if(commit == null)
      throw new IllegalStateException("No commit to amend");
    return commit;
  }

  private void prepareCommitter() {
    if(committer == null)
      committer = new PersonIdent(repository);
  }

  private void prepareAuthor() {
    if(author == null) {
      if(!amend)
        author = committer;
      else
        author = amendedCommit().getAuthorIdent();
    }
  }

  private void prepareParents() {
    if(parents == null) {
      if(!amend) {
        if(commit != null)
          parents = Collections.<AnyObjectId>singletonList(commit);
        else
          parents = Collections.emptyList();
      } else
        parents = Arrays.<AnyObjectId>asList(amendedCommit().getParents());
    }
  }

  private void updateRef(@Nonnull AnyObjectId head) throws IOException {
    if(amend)
      BranchUtils.amendCommit(branchRef, head, repository);
    else if(commit != null)
      BranchUtils.newCommit(branchRef, head, repository);
    else
      BranchUtils.initBranch(branchRef, head, repository);
  }

  private void updateFileSystem(@Nonnull RevCommit head) {
    gfs.setCommit(head);
  }

  @Nullable
  @Override
  public RevCommit doExecute() throws IOException {
    prepareCommitter();
    prepareAuthor();
    prepareParents();
    AnyObjectId tree = gfs.persist();
    if(!allowEmpty && !amend && tree.equals(commit.getTree()))
      return null;
    RevCommit resultCommit = CommitUtils.createCommit(message, tree, author, committer, parents, repository);
    if(branchRef != null)
      updateRef(resultCommit);
    updateFileSystem(resultCommit);
    return resultCommit;
  }
}
