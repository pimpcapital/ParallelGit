package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsFileStore;
import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.exceptions.*;
import com.beijunyi.parallelgit.filesystem.merge.GfsMergeNote;
import com.beijunyi.parallelgit.filesystem.merge.GfsMerger;
import com.beijunyi.parallelgit.utils.BranchUtils;
import com.beijunyi.parallelgit.utils.CommitUtils;
import com.beijunyi.parallelgit.utils.RefUtils;
import com.beijunyi.parallelgit.utils.exceptions.NoSuchBranchException;
import org.eclipse.jgit.api.MergeResult.MergeStatus;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.merge.MergeMessageFormatter;
import org.eclipse.jgit.merge.SquashMessageFormatter;
import org.eclipse.jgit.revwalk.RevCommit;

import static com.beijunyi.parallelgit.filesystem.GfsState.NORMAL;
import static com.beijunyi.parallelgit.filesystem.merge.GfsMergeNote.mergeSquash;
import static com.beijunyi.parallelgit.utils.RefUtils.ensureBranchRefName;
import static java.util.Collections.singletonList;
import static org.eclipse.jgit.api.MergeResult.MergeStatus.*;

public final class GfsMergeCommand extends GfsCommand<GfsMergeCommand.Result> {

  private String branch;
  private Ref branchRef;
  private RevCommit headCommit;
  private String target;

  private Ref targetRef;
  private RevCommit targetHeadCommit;
  private boolean squash = false;
  private boolean commit = true;

  private PersonIdent committer;
  private String message;
  private GfsMerger merger;

  public GfsMergeCommand(@Nonnull GitFileSystem gfs) {
    super(gfs);
  }

  @Nonnull
  @Override
  protected Result doExecute() throws IOException {
    prepareBranchHead();
    prepareTarget();
    prepareSourceCommit();
    prepareMessage();
    if(isUpToDate())
      return Result.upToDate(headCommit);
    if(canBeFastForwarded())
      return fastForward();
    return threeWayMerge();
  }

  @Nonnull
  public GfsMergeCommand target(@Nullable String branch) {
    this.target = branch;
    return this;
  }

  @Nonnull
  public GfsMergeCommand target(@Nullable Ref branchRef) {
    this.targetRef = branchRef;
    return this;
  }

  @Nonnull
  public GfsMergeCommand squash(boolean squash) {
    this.squash = squash;
    return this;
  }

  @Nonnull
  public GfsMergeCommand commit(boolean commit) {
    this.commit = commit;
    return this;
  }

  @Nonnull
  public GfsMergeCommand committer(@Nullable PersonIdent committer) {
    this.committer = committer;
    return this;
  }

  @Nonnull
  public GfsMergeCommand message(@Nullable String message) {
    this.message = message;
    return this;
  }

  @Nonnull
  public GfsMergeCommand setMerger(@Nullable GfsMerger merger) {
    this.merger = merger;
    return this;
  }

  private void prepareBranchHead() throws IOException {
    if(gfs.getStatusProvider().isDirty())
      throw new DirtyFileSystemException();

    branch = gfs.getStatusProvider().branch();
    if(branch == null)
      throw new NoBranchException();

    branchRef = RefUtils.getBranchRef(branch, repo);
    if(branchRef == null)
      throw new NoSuchBranchException(ensureBranchRefName(branch));

    headCommit = gfs.getStatusProvider().commit();
    if(headCommit == null)
      throw new NoHeadCommitException();
  }

  private void prepareTarget() throws IOException {
    if(targetRef == null) {
      targetRef = RefUtils.getBranchRef(target, repo);
    }
  }

  private void prepareSourceCommit() throws IOException {
    targetHeadCommit = CommitUtils.getCommit(targetRef, repo);
  }

  private void prepareMessage() throws IOException {
    if(message == null) {
      if(squash) {
        List<RevCommit> squashedCommits = CommitUtils.findSquashableCommits(targetHeadCommit, headCommit, repo);
        message = new SquashMessageFormatter().format(squashedCommits, branchRef);
      } else
        message = new MergeMessageFormatter().format(singletonList(targetRef), branchRef);
    }
  }

  private boolean isUpToDate() throws IOException {
    return CommitUtils.isMergedInto(headCommit, targetHeadCommit, repo);
  }

  private boolean canBeFastForwarded() throws IOException {
    return CommitUtils.isMergedInto(targetHeadCommit, headCommit, repo);
  }

  @Nonnull
  private Result fastForward() throws IOException {
    GfsStatusProvider status = gfs.getStatusProvider();
    Result result;
    if(squash) {
      GfsFileStore store = gfs.getFileStore();
      store.getRoot().reset(targetHeadCommit.getTree());
      status.mergeNote(mergeSquash(message));
      result = Result.fastForwardSquashed();
    } else {
      BranchUtils.mergeBranch(branch, targetHeadCommit, targetRef, FAST_FORWARD.toString(), repo);
      result = Result.fastForward(targetHeadCommit);
    }
    status.state(NORMAL);
    return result;
  }

  @Nonnull
  private Result threeWayMerge() throws IOException {
    prepareMerger();
    GfsStatusProvider status = gfs.getStatusProvider();
    if(merger.merge(headCommit, targetHeadCommit)) {
      if(commit && !squash) {
        AnyObjectId treeId = merger.getResultTreeId();
        prepareCommitter();
        RevCommit commit = CommitUtils.createCommit(message, treeId, committer, committer, Arrays.asList(headCommit, targetHeadCommit), repo);
        BranchUtils.mergeBranch(branch, commit, targetRef, "Merge made by recursive.", repo);
        status.state(NORMAL);
        return Result.merged(commit);
      }
    } else {
      List<String> conflictingPaths = new ArrayList<>(merger.getConflicts().keySet());
      Collections.sort(conflictingPaths);
      message = new MergeMessageFormatter().formatWithConflicts(message, conflictingPaths);
      return Result.conflicting();
    }
    if(squash) {
      status.mergeNote(GfsMergeNote.mergeSquash(message));
      status.state(NORMAL);
      return Result.mergedSquashed();
    }
    status.mergeNote(GfsMergeNote.mergeNoCommit(targetHeadCommit, message));
    return Result.mergedNotCommitted();
  }

  private void prepareMerger() {
    if(merger == null) {
      merger = new GfsMerger(gfs);
      merger.setConflictMarkers(Arrays.asList("BASE", "HEAD", targetRef.getName()));
    }
  }

  private void prepareCommitter() {
    if(committer == null)
      committer = new PersonIdent(repo);
  }

  public static class Result implements GfsCommandResult {

    private final MergeStatus status;
    private final RevCommit commit;

    private Result(@Nonnull MergeStatus status, @Nullable RevCommit commit) {
      this.status = status;
      this.commit = commit;
    }

    @Nonnull
    public static Result upToDate(@Nonnull RevCommit commit) {
      return new Result(ALREADY_UP_TO_DATE, commit);
    }

    @Nonnull
    public static Result fastForward(@Nonnull RevCommit commit) {
      return new Result(FAST_FORWARD, commit);
    }

    @Nonnull
    public static Result fastForwardSquashed() {
      return new Result(FAST_FORWARD_SQUASHED, null);
    }

    @Nonnull
    public static Result merged(@Nonnull RevCommit commit) {
      return new Result(MERGED, commit);
    }

    @Nonnull
    public static Result mergedSquashed() {
      return new Result(MERGED_SQUASHED, null);
    }

    @Nonnull
    public static Result mergedNotCommitted() {
      return new Result(MERGED_NOT_COMMITTED, null);
    }

    @Nonnull
    public static Result conflicting() {
      return new Result(CONFLICTING, null);
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
