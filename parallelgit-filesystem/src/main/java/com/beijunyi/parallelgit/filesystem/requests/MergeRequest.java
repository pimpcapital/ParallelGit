package com.beijunyi.parallelgit.filesystem.requests;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.exceptions.DirtyFileSystemException;
import com.beijunyi.parallelgit.utils.CommitUtils;
import com.beijunyi.parallelgit.utils.RefUtils;
import com.beijunyi.parallelgit.utils.exceptions.NoHeadCommitException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

public final class MergeRequest extends GitFileSystemRequest<RevCommit> {

  private RevCommit headCommit;

  private String branch;
  private Ref branchRef;
  private RevCommit srcCommit;

  private boolean squash = false;
  private String message;

  public MergeRequest(@Nonnull GitFileSystem gfs) {
    super(gfs);
  }

  @Nonnull
  public MergeRequest branch(@Nullable String branch) {
    this.branch = branch;
    return this;
  }

  @Nonnull
  public MergeRequest branch(@Nullable Ref branchRef) {
    this.branchRef = branchRef;
    return this;
  }

  @Nonnull
  public MergeRequest squash(boolean squash) {
    this.squash = squash;
    return this;
  }

  @Nonnull
  public MergeRequest message(@Nullable String message) {
    this.message = message;
    return this;
  }

  @Nullable
  @Override
  protected RevCommit doExecute() throws IOException {
    prepareHeadCommit();
    prepareBranch();
    prepareSourceCommit();
    if(isUpToDate())
      return headCommit;
    if(canBeFastForwarded())
      return fastForward();
    return threeWayMerge();
  }

  private void prepareHeadCommit() {
    if(gfs.isDirty())
      throw new DirtyFileSystemException();
    if(gfs.getCommit() == null)
      throw new NoHeadCommitException();
    headCommit = gfs.getCommit();
  }

  private void prepareBranch() throws IOException {
    if(branchRef == null) {
      branchRef = RefUtils.getBranchRef(branch, repo);
    }
  }

  private void prepareSourceCommit() throws IOException {
    srcCommit = CommitUtils.getCommit(branchRef, repo);
  }

  private boolean isUpToDate() throws IOException {
    return CommitUtils.isMergedInto(headCommit, srcCommit, repo);
  }

  private boolean canBeFastForwarded() throws IOException {
    return CommitUtils.isMergedInto(srcCommit, headCommit, repo);
  }

  @Nonnull
  private RevCommit fastForward() {

    return null;
  }

  @Nullable
  private RevCommit threeWayMerge() {
    return null;
  }

}
