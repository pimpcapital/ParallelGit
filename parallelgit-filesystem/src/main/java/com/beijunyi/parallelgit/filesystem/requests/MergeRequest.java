package com.beijunyi.parallelgit.filesystem.requests;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.exceptions.DirtyFileSystemException;
import com.beijunyi.parallelgit.filesystem.exceptions.NoBranchException;
import com.beijunyi.parallelgit.filesystem.exceptions.NoHeadCommitException;
import com.beijunyi.parallelgit.filesystem.merge.GfsMerger;
import com.beijunyi.parallelgit.utils.BranchUtils;
import com.beijunyi.parallelgit.utils.CommitUtils;
import com.beijunyi.parallelgit.utils.RefUtils;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.merge.SquashMessageFormatter;
import org.eclipse.jgit.revwalk.RevCommit;

public final class MergeRequest extends GitFileSystemRequest<RevCommit> {

  private String branch;
  private RevCommit headCommit;
  private String target;

  private Ref targetRef;
  private RevCommit targetHeadCommit;
  private boolean squash = false;
  private boolean commit = true;

  private String message;
  private GfsMerger merger;

  public MergeRequest(@Nonnull GitFileSystem gfs) {
    super(gfs);
  }

  @Nonnull
  public MergeRequest target(@Nullable String branch) {
    this.target = branch;
    return this;
  }

  @Nonnull
  public MergeRequest target(@Nullable Ref branchRef) {
    this.targetRef = branchRef;
    return this;
  }

  @Nonnull
  public MergeRequest squash(boolean squash) {
    this.squash = squash;
    return this;
  }

  @Nonnull
  public MergeRequest commit(boolean commit) {
    this.commit = commit;
    return this;
  }

  @Nonnull
  public MergeRequest message(@Nullable String message) {
    this.message = message;
    return this;
  }

  @Nonnull
  public MergeRequest setMerger(@Nullable GfsMerger merger) {
    this.merger = merger;
    return this;
  }

  @Nullable
  @Override
  protected RevCommit doExecute() throws IOException {
    prepareBranchHead();
    prepareTarget();
    prepareSourceCommit();
    if(isUpToDate())
      return headCommit;
    if(canBeFastForwarded())
      return fastForward();
    return threeWayMerge();
  }

  private void prepareBranchHead() throws IOException {
    if(gfs.isDirty())
      throw new DirtyFileSystemException();

    branch = gfs.getBranch();
    if(branch == null)
      throw new NoBranchException();

    headCommit = gfs.getCommit();
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

  private boolean isUpToDate() throws IOException {
    return CommitUtils.isMergedInto(headCommit, targetHeadCommit, repo);
  }

  private boolean canBeFastForwarded() throws IOException {
    return CommitUtils.isMergedInto(targetHeadCommit, headCommit, repo);
  }

  @Nonnull
  private RevCommit fastForward() throws IOException {
    if(squash) {
      if(message == null) {
        List<RevCommit> squashedCommits = CommitUtils.findSquashableCommits(targetHeadCommit, headCommit, repo);
        message = new SquashMessageFormatter().format(squashedCommits, targetRef);
      }
      AnyObjectId tree = gfs.persist();
      RevCommit newCommit = CommitUtils.createCommit(message, tree, headCommit, repo);
      BranchUtils.newCommit(branch, newCommit, repo);
      return newCommit;
    } else {
      BranchUtils.mergeBranch(branch, targetHeadCommit, target, "Fast-forward", repo);
      return targetHeadCommit;
    }
  }

  @Nullable
  private RevCommit threeWayMerge() throws IOException {
    merger.setConflictMarkers(Arrays.asList("BASE", "HEAD", targetRef.getName()));
    if(merger.merge(headCommit, targetHeadCommit)) {

    } else {

    }
    return null;
  }

}
