package com.beijunyi.parallelgit.filesystem;

import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.merge.GfsMergeNote;
import org.eclipse.jgit.revwalk.RevCommit;

public class GfsStatusUpdater {

  private String branch;
  private RevCommit commit;
  private GfsMergeNote mergeNote;
  private boolean clearMergeNote;

  GfsStatusUpdater() {}

  public void setBranch(@Nonnull String branch) {
    this.branch = branch;
  }

  public void setCommit(@Nonnull RevCommit commit) {
    this.commit = commit;
  }

  public void setMergeNote(@Nonnull GfsMergeNote mergeNote) {
    if(clearMergeNote)
      throw new IllegalStateException();
    this.mergeNote = mergeNote;
  }

  public void clearMergeNote() {
    if(mergeNote != null)
      throw new IllegalStateException();
    clearMergeNote = true;
  }

  @Nonnull
  public GfsStatus update(@Nonnull GfsStatus status) {
    String newBranch = branch != null ? branch : status.branch();
    RevCommit newCommit = commit != null ? commit : status.commit();
    GfsMergeNote newMergeNote = clearMergeNote ? null : mergeNote != null ? mergeNote : status.mergeNote();
    return new GfsStatus(status.state(), newBranch, newCommit, newMergeNote);
  }

}
