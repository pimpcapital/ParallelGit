package com.beijunyi.parallelgit.filesystem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.merge.GfsMergeNote;
import org.eclipse.jgit.revwalk.RevCommit;

public class GfsStatusUpdate implements AutoCloseable {

  private final GfsStatusProvider provider;

  private GfsState state;
  private String branch;
  private RevCommit commit;
  private GfsMergeNote mergeNote;

  private boolean closed = false;

  public GfsStatusUpdate(@Nonnull GfsStatusProvider provider) {
    this.provider = provider;
    readStatus();
  }

  @Override
  public synchronized void close() {
    if(!closed)
      provider.completeUpdate(this);
  }

  @Nonnull
  public GfsState getState() {
    return state;
  }

  public void setState(@Nonnull GfsState state) {
    this.state = state;
  }

  @Nullable
  public String getBranch() {
    return branch;
  }

  public void setBranch(@Nullable String branch) {
    this.branch = branch;
  }

  @Nullable
  public RevCommit getCommit() {
    return commit;
  }

  public void setCommit(@Nullable RevCommit commit) {
    this.commit = commit;
  }

  @Nullable
  public GfsMergeNote getMergeNote() {
    return mergeNote;
  }

  public void setMergeNote(@Nullable GfsMergeNote mergeNote) {
    this.mergeNote = mergeNote;
  }

  private void readStatus() {
    branch = provider.branch();
    commit = provider.commit();
    state = provider.state();
    mergeNote = provider.mergeNote();
  }
}
