package com.beijunyi.parallelgit.filesystem.requests;

import java.io.IOException;
import java.util.ArrayList;
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
import com.beijunyi.parallelgit.utils.exceptions.NoSuchBranchException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.merge.MergeMessageFormatter;
import org.eclipse.jgit.merge.SquashMessageFormatter;
import org.eclipse.jgit.revwalk.RevCommit;

import static com.beijunyi.parallelgit.utils.RefUtils.ensureBranchRefName;
import static java.util.Collections.singletonList;

public final class MergeRequest extends GitFileSystemRequest<RevCommit> {

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
  public MergeRequest committer(@Nullable PersonIdent committer) {
    this.committer = committer;
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
    prepareMessage();
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

    branchRef = RefUtils.getBranchRef(branch, repo);
    if(branchRef == null)
      throw new NoSuchBranchException(ensureBranchRefName(branch));

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

}
