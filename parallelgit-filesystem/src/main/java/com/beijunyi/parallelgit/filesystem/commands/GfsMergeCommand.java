package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.*;
import com.beijunyi.parallelgit.filesystem.exceptions.*;
import com.beijunyi.parallelgit.filesystem.merge.GfsMerger;
import com.beijunyi.parallelgit.utils.BranchUtils;
import com.beijunyi.parallelgit.utils.CommitUtils;
import com.beijunyi.parallelgit.utils.RefUtils;
import com.beijunyi.parallelgit.utils.exceptions.NoSuchBranchException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.merge.MergeMessageFormatter;
import org.eclipse.jgit.merge.SquashMessageFormatter;
import org.eclipse.jgit.revwalk.RevCommit;

import static com.beijunyi.parallelgit.utils.RefUtils.ensureBranchRefName;
import static java.util.Collections.singletonList;

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
      return Result.success(headCommit);
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
    if(gfs.isDirty())
      throw new DirtyFileSystemException();

    branch = gfs.status().branch();
    if(branch == null)
      throw new NoBranchException();

    branchRef = RefUtils.getBranchRef(branch, repo);
    if(branchRef == null)
      throw new NoSuchBranchException(ensureBranchRefName(branch));

    headCommit = gfs.status().commit();
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

  @Nullable
  private RevCommit fastForward() throws IOException {
    if(squash) {
      gfs.setMessage(message);
      return null;
    } else {
      BranchUtils.mergeBranch(branch, targetHeadCommit, targetRef, "Fast-forward", repo);
      return targetHeadCommit;
    }
  }

  @Nullable
  private RevCommit threeWayMerge() throws IOException {
    prepareMerger();
    if(merger.merge(headCommit, targetHeadCommit)) {
      if(commit && !squash) {
        AnyObjectId treeId = merger.getResultTreeId();
        prepareCommitter();
        RevCommit commit = CommitUtils.createCommit(message, treeId, committer, committer, Arrays.asList(headCommit, targetHeadCommit), repo);
        BranchUtils.mergeBranch(branch, commit, targetRef, "Merge made by recursive.", repo);
        return commit;
      }
    } else {
      List<String> conflictingPaths = new ArrayList<>(merger.getConflicts().keySet());
      message = new MergeMessageFormatter().formatWithConflicts(message, conflictingPaths);
    }
    if(!squash)
      gfs.setSourceCommit(targetHeadCommit);
    gfs.setMessage(message);
    return null;
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
