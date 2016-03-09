package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GfsState;
import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.exceptions.NoBranchException;
import com.beijunyi.parallelgit.filesystem.exceptions.NoHeadCommitException;
import com.beijunyi.parallelgit.utils.BranchUtils;
import org.eclipse.jgit.revwalk.RevCommit;

import static com.beijunyi.parallelgit.filesystem.GfsState.RESETTING;
import static com.beijunyi.parallelgit.filesystem.commands.GfsResetCommand.Status.SUCCESS;
import static com.beijunyi.parallelgit.utils.CommitUtils.getCommit;

public class GfsResetCommand extends GfsCommand<GfsResetCommand.Result> {

  private boolean soft = false;
  private String branch;
  private String revision;

  public GfsResetCommand(@Nonnull GitFileSystem gfs) {
    super(gfs);
  }

  @Nonnull
  public GfsResetCommand soft(boolean soft) {
    this.soft = soft;
    return this;
  }

  @Nonnull
  public GfsResetCommand revision(@Nonnull String revision) {
    this.revision = revision;
    return this;
  }

  @Nonnull
  @Override
  protected GfsState getCommandState() {
    return RESETTING;
  }

  @Nonnull
  @Override
  protected Result doExecute(@Nonnull GfsStatusProvider.Update update) throws IOException {
    prepareBranch();
    prepareCommit();
    RevCommit commit = getCommit(revision, repo);
    BranchUtils.resetBranchHead(branch, commit, repo);
    gfs.updateOrigin(commit.getTree());
    if(!soft)
      gfs.reset();
    return Result.success();
  }

  private void prepareBranch() {
    if(!status.isAttached())
      throw new NoBranchException();
    branch = status.branch();
  }

  private void prepareCommit() throws IOException {
    if(revision == null) {
      if(!status.isInitialized())
        throw new NoHeadCommitException();
      revision = status.commit().getName();
    }
  }

  public enum Status {
    SUCCESS,
  }

  public static class Result implements GfsCommandResult {

    private final Status status;

    public Result(@Nonnull Status status) {
      this.status = status;
    }

    @Nonnull
    public static Result success() {
      return new Result(SUCCESS);
    }

    @Override
    public boolean isSuccessful() {
      return SUCCESS.equals(status);
    }

  }

}
