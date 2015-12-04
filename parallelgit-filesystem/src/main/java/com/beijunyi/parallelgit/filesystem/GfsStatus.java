package com.beijunyi.parallelgit.filesystem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.exceptions.MergeNotStartedException;
import com.beijunyi.parallelgit.filesystem.exceptions.NoBranchException;
import com.beijunyi.parallelgit.filesystem.exceptions.NoHeadCommitException;
import com.beijunyi.parallelgit.filesystem.merge.GfsMergeNote;
import org.eclipse.jgit.revwalk.RevCommit;

public class GfsStatus {

  private final GfsState state;
  private final String branch;
  private final RevCommit commit;
  private final GfsMergeNote mergeNote;

  public GfsStatus(@Nonnull GfsState state, @Nullable String branch, @Nullable RevCommit commit, @Nullable GfsMergeNote mergeNote) {
    this.state = state;
    this.branch = branch;
    this.commit = commit;
    this.mergeNote = mergeNote;
  }

  @Nonnull
  public GfsState state() {
    return state;
  }

  @Nonnull
  public String branch() {
    if(branch == null)
      throw new NoBranchException();
    return branch;
  }

  public boolean isAttached() {
    return branch != null;
  }

  @Nonnull
  public RevCommit commit() {
    if(commit == null)
      throw new NoHeadCommitException();
    return commit;
  }

  public boolean isInitialized() {
    return commit != null;
  }

  @Nonnull
  public GfsMergeNote mergeNote() {
    if(mergeNote == null)
      throw new MergeNotStartedException();
    return mergeNote;
  }

}
