package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsState;
import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.exceptions.BadGfsStateException;
import com.beijunyi.parallelgit.filesystem.exceptions.NoChangeException;
import com.beijunyi.parallelgit.utils.BranchUtils;
import com.beijunyi.parallelgit.utils.CommitUtils;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import static com.beijunyi.parallelgit.filesystem.GfsState.COMMITTING;

public final class GfsCommitCommand extends GfsCommand<GfsCommitCommand.Result> {

  private PersonIdent author;
  private PersonIdent committer;
  private String message;
  private List<? extends AnyObjectId> parents;
  private boolean amend = false;
  private boolean allowEmpty = false;

  public GfsCommitCommand(@Nonnull GitFileSystem gfs) {
    super(gfs);
  }

  @Nonnull
  @Override
  protected Result doExecute() throws IOException {
    prepareState();
    prepareMessage();
    prepareCommitter();
    prepareAuthor();
    prepareParents();
    AnyObjectId resultTree = gfs.flush();
    if(!allowEmpty && !amend && isSameAsParent(resultTree))
      return Result.noChange();
    RevCommit resultCommit = CommitUtils.createCommit(message, resultTree, author, committer, parents, repo);
    updateStatus(resultCommit);
    return Result.success(resultCommit);
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

  private void prepareState() {
    try(GfsStatusProvider.Update update = status.prepareUpdate()) {
      GfsState state  = status.state();
      switch(state) {
        case NORMAL:
          update.state(COMMITTING);
        case MERGING:
        case CHERRY_PICKING:
          break;
        default:
          throw new BadGfsStateException(state);
      }
    }

  }

  private void prepareMessage() {
    if(message == null) {
      if(status.state() == GfsState.MERGING || status.state() == GfsState.CHERRY_PICKING)
        message = status.mergeNote().getMessage();
      else
        message = "";
    }
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
        author = status.commit().getAuthorIdent();
    }
  }

  private void prepareParents() {
    if(parents == null) {
      if(!amend) {
        if(status.isInitialized())
          parents = Collections.singletonList(status.commit());
        else
          parents = Collections.emptyList();
      } else
        parents = Arrays.asList(status.commit().getParents());
    }
  }

  private boolean isSameAsParent(@Nonnull AnyObjectId newTree) {
    return status.isInitialized() && status.commit().getTree().equals(newTree);
  }

  private void updateStatus(@Nonnull RevCommit newHead) throws IOException {
    if(status.isAttached()) {
      if(amend)
        BranchUtils.amendCommit(status.branch(), newHead, repo);
      else if(status.isInitialized())
        BranchUtils.newCommit(status.branch(), newHead, repo);
      else
        BranchUtils.initBranch(status.branch(), newHead, repo);
    }
    try(GfsStatusProvider.Update update = status.prepareUpdate()) {
      update.commit(newHead);
      update.clearMergeNote();
    }
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
