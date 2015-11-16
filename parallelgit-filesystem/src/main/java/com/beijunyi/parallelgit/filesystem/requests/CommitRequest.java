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
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import static com.beijunyi.parallelgit.utils.RefUtils.ensureBranchRefName;

public final class CommitRequest extends GitFileSystemRequest<RevCommit> {

  private final String branchRef;
  private final RevCommit commit;
  private PersonIdent author;
  private PersonIdent committer;
  private String message;
  private List<? extends AnyObjectId> parents;
  private boolean amend = false;
  private boolean allowEmpty = false;

  public CommitRequest(@Nonnull GitFileSystem gfs) {
    super(gfs);
    String branch = gfs.getBranch();
    branchRef = branch != null ? ensureBranchRefName(branch) : null;
    commit = gfs.getCommit();
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

  @Nullable
  @Override
  protected RevCommit doExecute() throws IOException {
    prepareCommitter();
    prepareAuthor();
    prepareParents();
    AnyObjectId tree = gfs.persist();
    if(!allowEmpty && !amend && tree.equals(commit.getTree()))
      return null;
    RevCommit resultCommit = CommitUtils.createCommit(message, tree, author, committer, parents, repo);
    if(branchRef != null)
      updateRef(resultCommit);
    updateFileSystem(resultCommit);
    return resultCommit;
  }

  @Nonnull
  private RevCommit amendedCommit() {
    if(commit == null)
      throw new IllegalStateException("No commit to amend");
    return commit;
  }

  private void prepareCommitter() {
    if(committer == null)
      committer = new PersonIdent(repo);
  }

  private void prepareMessage() {
    if(message == null)
      message = gfs.getMessage();
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
          parents = Collections.singletonList(commit);
        else
          parents = Collections.emptyList();
      } else
        parents = Arrays.asList(amendedCommit().getParents());
    }
  }

  private void updateRef(@Nonnull AnyObjectId head) throws IOException {
    if(amend)
      BranchUtils.amendCommit(branchRef, head, repo);
    else if(commit != null)
      BranchUtils.newCommit(branchRef, head, repo);
    else
      BranchUtils.initBranch(branchRef, head, repo);
  }

  private void updateFileSystem(@Nonnull RevCommit head) {
    gfs.setCommit(head);
    gfs.setMessage(null);
  }
}
