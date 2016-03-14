package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsState;
import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.exceptions.NoBranchException;
import com.beijunyi.parallelgit.filesystem.exceptions.NoHeadCommitException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import static com.beijunyi.parallelgit.filesystem.commands.GfsCreateStash.Result.noChange;
import static com.beijunyi.parallelgit.utils.CommitUtils.*;
import static java.util.Collections.singletonList;

public class GfsCreateStash extends GfsCommand<GfsCreateStash.Result> {

  private static final String DEFAULT_INDEX_MESSAGE_FORMAT = "index on {0}: {1} {2}";
  private static final String DEFAULT_WORKING_DIR_MESSAGE_FORMAT = "WIP on {0}: {1} {2}";

  private String branch;
  private String indexMessage;
  private String workingDirectoryMessage;
  private PersonIdent committer;
  private AnyObjectId parent;

  public GfsCreateStash(@Nonnull GitFileSystem gfs) {
    super(gfs);
  }

  @Nonnull
  @Override
  protected GfsState getCommandState() {
    return GfsState.CREATING_STASH;
  }

  @Nonnull
  public GfsCreateStash indexMessage(@Nonnull String indexMessage) {
    this.indexMessage = indexMessage;
    return this;
  }

  @Nonnull
  public GfsCreateStash committer(@Nonnull PersonIdent committer) {
    this.committer = committer;
    return this;
  }

  @Nonnull
  public GfsCreateStash parent(@Nonnull AnyObjectId parent) {
    this.parent = parent;
    return this;
  }

  @Nonnull
  @Override
  protected GfsCreateStash.Result doExecute(@Nonnull GfsStatusProvider.Update update) throws IOException {
    prepareBranch();
    prepareCommitter();
    prepareParent();
    prepareIndexMessage();
    prepareDirectoryMessage();
    AnyObjectId resultTree = gfs.flush();
    if(parent != null && parent.equals(resultTree))
      return noChange();
    RevCommit indexCommit = makeIndexCommit(resultTree);
    RevCommit workingDirectoryCommit = makeWorkingDirectoryCommit(indexCommit);
    resetHead();
    return Result.success(workingDirectoryCommit);
  }

  private void prepareBranch() {
    if(!status.isAttached())
      throw new NoBranchException();
    branch = Repository.shortenRefName(status.branch());
  }

  private void prepareCommitter() {
    if(committer == null)
      committer = new PersonIdent(repo);
  }

  private void prepareParent() {
    if(!status.isInitialized())
      throw new NoHeadCommitException();
    parent = status.commit();
  }

  private void prepareIndexMessage() throws IOException {
    if(indexMessage == null)
      indexMessage = MessageFormat.format(DEFAULT_INDEX_MESSAGE_FORMAT, branch, parent.abbreviate(7).name(), getCommit(parent, repo).getShortMessage());
  }

  private void prepareDirectoryMessage() throws IOException {
    if(workingDirectoryMessage == null)
      workingDirectoryMessage = MessageFormat.format(DEFAULT_WORKING_DIR_MESSAGE_FORMAT, branch, parent.abbreviate(7).name(), getCommit(parent, repo).getShortMessage());
  }

  @Nonnull
  private RevCommit makeIndexCommit(@Nonnull AnyObjectId tree) throws IOException {
    List<AnyObjectId> parents = singletonList(parent);
    return createCommit(indexMessage, tree, committer, committer, parents, repo);
  }

  @Nonnull
  private RevCommit makeWorkingDirectoryCommit(@Nonnull RevCommit indexCommit) throws IOException {
    AnyObjectId tree = indexCommit.getTree();
    List<AnyObjectId> parents = Arrays.asList(parent, indexCommit);
    return createCommit(workingDirectoryMessage, tree, committer, committer, parents, repo);
  }

  private void resetHead() throws IOException {
    gfs.reset();
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
