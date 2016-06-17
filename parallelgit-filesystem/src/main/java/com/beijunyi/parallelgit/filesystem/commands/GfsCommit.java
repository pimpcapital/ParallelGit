package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.exceptions.UnsuccessfulOperationException;
import com.beijunyi.parallelgit.filesystem.merge.MergeNote;
import com.beijunyi.parallelgit.utils.BranchUtils;
import com.beijunyi.parallelgit.utils.CommitUtils;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import static java.util.Arrays.asList;
import static java.util.Collections.*;

public class GfsCommit extends GfsCommand<GfsCommit.Result> {

  private PersonIdent author;
  private PersonIdent committer;
  private String message;
  private List<? extends AnyObjectId> parents;
  private boolean amend = false;
  private boolean allowEmpty = false;

  public GfsCommit(GitFileSystem gfs) {
    super(gfs);
  }

  @Nonnull
  @Override
  protected Result doExecute(GfsStatusProvider.Update update) throws IOException {
    prepareMessage();
    prepareCommitter();
    prepareAuthor();
    prepareParents();
    ObjectId resultTree = gfs.flush();
    gfs.updateOrigin(resultTree);
    if(!allowEmpty && !amend && isSameAsParent(resultTree))
      return Result.noChange();
    RevCommit resultCommit = CommitUtils.createCommit(message, resultTree, author, committer, parents, repo);
    updateStatus(update, resultCommit);
    return Result.success(resultCommit);
  }

  @Nonnull
  public GfsCommit author(@Nullable PersonIdent author) {
    this.author = author;
    return this;
  }

  @Nonnull
  public GfsCommit committer(@Nullable PersonIdent committer) {
    this.committer = committer;
    return this;
  }

  @Nonnull
  public GfsCommit message(@Nullable String message) {
    this.message = message;
    return this;
  }

  @Nonnull
  public GfsCommit amend(boolean amend) {
    this.amend = amend;
    return this;
  }

  @Nonnull
  public GfsCommit allowEmpty(boolean allowEmpty) {
    this.allowEmpty = allowEmpty;
    return this;
  }

  private void prepareMessage() {
    if(message == null) {
      MergeNote note = status.mergeNote();
      message = note != null ? note.getMessage() : "";
    }
  }

  private void prepareCommitter() {
    if(committer == null) committer = new PersonIdent(repo);
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
        MergeNote mergeNote = status.mergeNote();
        if(mergeNote != null && mergeNote.getSource() != null) {
          parents = asList(status.commit(), mergeNote.getSource());
        } else if(status.isInitialized()) {
          parents = singletonList(status.commit());
        } else {
          parents = emptyList();
        }
      } else {
        parents = asList(status.commit().getParents());
      }
    }
  }

  private boolean isSameAsParent(AnyObjectId newTree) {
    return status.isInitialized() && status.commit().getTree().equals(newTree);
  }

  private void updateStatus(GfsStatusProvider.Update update, RevCommit newHead) throws IOException {
    if(status.isAttached()) {
      MergeNote mergeNote = status.mergeNote();
      if(!amend) {
        if(mergeNote != null) {
          BranchUtils.mergeCommit(status.branch(), newHead, repo);
        } else if(status.isInitialized()) {
          BranchUtils.newCommit(status.branch(), newHead, repo);
        } else {
          BranchUtils.initBranch(status.branch(), newHead, repo);
        }
      } else {
        BranchUtils.amendCommit(status.branch(), newHead, repo);
      }
    }
    update.commit(newHead);
    update.clearMergeNote();
  }

  public enum Status {
    COMMITTED,
    NO_CHANGE
  }

  public static class Result implements GfsCommandResult {

    private final Status status;
    private final RevCommit commit;

    private Result(Status status, @Nullable RevCommit commit) {
      this.status = status;
      this.commit = commit;
    }

    @Nonnull
    public static Result success(RevCommit commit) {
      return new Result(Status.COMMITTED, commit);
    }

    @Nonnull
    public static Result noChange() {
      return new Result(Status.NO_CHANGE, null);
    }

    @Override
    public boolean isSuccessful() {
      return commit != null;
    }

    @Nonnull
    public RevCommit getCommit() {
      if(commit == null) throw new UnsuccessfulOperationException();
      return commit;
    }

    @Nonnull
    public Status getStatus() {
      return status;
    }
  }

}
