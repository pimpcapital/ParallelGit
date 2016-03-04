package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import java.util.Collections;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsState;
import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.utils.CommitUtils;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

public class GfsStashCommand extends GfsCommand<GfsStashCommand.Result> {

  private String message = "index on {0}: {1} {2}";
  private PersonIdent committer;
  private AnyObjectId parent;

  public GfsStashCommand(@Nonnull GitFileSystem gfs) {
    super(gfs);
  }

  @Nonnull
  @Override
  protected GfsState getCommandState() {
    return GfsState.STASHING;
  }

  @Nonnull
  public GfsStashCommand setMessage(@Nonnull String message) {
    this.message = message;
    return this;
  }

  @Nonnull
  public GfsStashCommand setCommitter(@Nonnull PersonIdent committer) {
    this.committer = committer;
    return this;
  }

  @Nonnull
  public GfsStashCommand setParent(@Nonnull AnyObjectId parent) {
    this.parent = parent;
    return this;
  }

  @Nonnull
  @Override
  protected GfsStashCommand.Result doExecute(@Nonnull GfsStatusProvider.Update update) throws IOException {
    prepareMessage();
    prepareCommitter();
    prepareParent();
    AnyObjectId resultTree = gfs.flush();
    if(parent != null && parent.equals(resultTree))
      return Result.noChange();
    RevCommit resultCommit = CommitUtils.createCommit(message, resultTree, committer, committer, Collections.singletonList(parent), repo);
    resetHead();
    return Result.success(resultCommit);
  }

  private void prepareMessage() {

  }

  private void prepareCommitter() {

  }

  private void prepareParent() {

  }

  private void resetHead() {

  }

  public enum Status {
    COMMITTED,
    NO_CHANGE
  }

  public static class Result implements GfsCommandResult {

    private final Status status;
    private final RevCommit commit;

    public Result(@Nonnull Status status, @Nullable RevCommit commit) {
      this.status = status;
      this.commit = commit;
    }

    @Override
    public boolean isSuccessful() {
      return false;
    }

    @Nonnull
    public static Result success(@Nonnull RevCommit commit) {
      return new Result(Status.COMMITTED, commit);
    }

    @Nonnull
    public static Result noChange() {
      return new Result(Status.NO_CHANGE, null);
    }

  }

}
