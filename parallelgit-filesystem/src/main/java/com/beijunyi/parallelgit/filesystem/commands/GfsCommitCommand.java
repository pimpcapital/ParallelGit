package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsState;
import com.beijunyi.parallelgit.filesystem.GfsStatusUpdate;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.exceptions.NoChangeException;
import com.beijunyi.parallelgit.utils.BranchUtils;
import com.beijunyi.parallelgit.utils.CommitUtils;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import static com.beijunyi.parallelgit.utils.RefUtils.ensureBranchRefName;

public final class GfsCommitCommand extends GfsCommand<GfsCommitCommand.Result> {

  private final String branchRef;
  private final RevCommit commit;
  private PersonIdent author;
  private PersonIdent committer;
  private String message;
  private List<? extends AnyObjectId> parents;
  private boolean amend = false;
  private boolean allowEmpty = false;

  public GfsCommitCommand(@Nonnull GitFileSystem gfs) {
    super(gfs);
    String branch = gfs.getBranch();
    branchRef = branch != null ? ensureBranchRefName(branch) : null;
    commit = gfs.getCommit();
  }

  @Nonnull
  @Override
  protected GfsState startState() {
    return GfsState.COMMITTING;
  }

  @Nonnull
  @Override
  protected Result doExecute(@Nonnull GfsStatusUpdate status) throws IOException {
    prepareMessage();
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
  public GfsCommitCommand author(@Nullable PersonIdent author) {
    this.author = author;
    return this;
  }

  @Nonnull
  public GfsCommitCommand committer(@Nullable PersonIdent committer) {
    this.committer = committer;
    return this;
  }

  @Nonnull
  public GfsCommitCommand message(@Nullable String message) {
    this.message = message;
    return this;
  }

  @Nonnull
  public GfsCommitCommand amend(boolean amend) {
    this.amend = amend;
    return this;
  }

  @Nonnull
  public GfsCommitCommand allowEmpty(boolean allowEmpty) {
    this.allowEmpty = allowEmpty;
    return this;
  }

  @Nonnull
  private RevCommit amendedCommit() {
    if(commit == null)
      throw new IllegalStateException("No commit to amend");
    return commit;
  }

  private void prepareMessage() {
    if(message == null)
      message = gfs.getMessage();
  }

  private void prepareCommitter() {
    if(committer == null)
      committer = new PersonIdent(repo);
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

  public static class Result implements GfsCommandResult {

    private final RevCommit commit;

    private Result(@Nullable RevCommit commit) {
      this.commit = commit;
    }

    @Nonnull
    public static Result success(@Nonnull RevCommit commit) {
      return new Result(commit);
    }

    @Nonnull
    public static Result noChange() {
      return new Result(null);
    }

    @Override
    public boolean isSuccessful() {
      return commit != null;
    }

    @Nonnull
    public RevCommit getCommit() {
      if(commit == null)
        throw new NoChangeException();
      return commit;
    }

  }
}
