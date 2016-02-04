package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsState;
import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.exceptions.UnsuccessfulOperationException;
import com.beijunyi.parallelgit.utils.BranchUtils;
import com.beijunyi.parallelgit.utils.CommitUtils;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import static com.beijunyi.parallelgit.filesystem.GfsState.*;

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
  protected Result doExecute(@Nonnull GfsStatusProvider.Update update) throws IOException {
    if(status.isAttached()) {
      if(!isHeadSynchonized()) {
        update.state(NORMAL);
        return Result.outOfSync();
      }
    }
    prepareMessage();
    prepareCommitter();
    prepareAuthor();
    prepareParents();
    AnyObjectId resultTree = gfs.flush();
    if(!allowEmpty && !amend && isSameAsParent(resultTree))
      return Result.noChange();
    RevCommit resultCommit = CommitUtils.createCommit(message, resultTree, author, committer, parents, repo);
    updateStatus(update, resultCommit);
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

  @Nonnull
  @Override
  protected EnumSet<GfsState> getAcceptableStates() {
    return EnumSet.of(NORMAL, MERGING_CONFLICT, CHERRY_PICKING_CONFLICT);
  }

  @Nonnull
  @Override
  protected GfsState getCommandState() {
    return GfsState.COMMITTING;
  }

  private boolean isHeadSynchonized() throws IOException {
    if(BranchUtils.branchExists(status.branch(), repo)) {
      RevCommit head = BranchUtils.getHeadCommit(status.branch(), repo);
      return head.equals(status.commit());
    }
    return true;
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

  private void updateStatus(@Nonnull GfsStatusProvider.Update update, @Nonnull RevCommit newHead) throws IOException {
    if(status.isAttached()) {
      if(amend)
        BranchUtils.amendCommit(status.branch(), newHead, repo);
      else if(status.isInitialized())
        BranchUtils.newCommit(status.branch(), newHead, repo);
      else
        BranchUtils.initBranch(status.branch(), newHead, repo);
    }
    update.commit(newHead);
    update.clearMergeNote();
  }

  public enum CommitStatus {
    COMMITTED,
    NO_CHANGE,
    OUT_OF_SYNC
  }

  public static class Result implements GfsCommandResult {

    private final RevCommit commit;
    private final CommitStatus status;

    private Result(@Nullable RevCommit commit, @Nonnull CommitStatus status) {
      this.commit = commit;
      this.status = status;
    }

    @Nonnull
    public static Result success(@Nonnull RevCommit commit) {
      return new Result(commit, CommitStatus.COMMITTED);
    }

    @Nonnull
    public static Result noChange() {
      return new Result(null, CommitStatus.NO_CHANGE);
    }

    @Nonnull
    public static Result outOfSync() {
      return new Result(null, CommitStatus.OUT_OF_SYNC);
    }

    @Override
    public boolean isSuccessful() {
      return commit != null;
    }

    @Nonnull
    public RevCommit getCommit() {
      if(commit == null)
        throw new UnsuccessfulOperationException();
      return commit;
    }

    @Nonnull
    public CommitStatus getStatus() {
      return status;
    }
  }

}
