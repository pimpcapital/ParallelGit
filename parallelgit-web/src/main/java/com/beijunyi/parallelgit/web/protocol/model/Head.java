package com.beijunyi.parallelgit.web.protocol.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;

public class Head {

  private final String branch;
  private final CommitView commit;

  private Head(@Nullable String branch, @Nullable CommitView commit) {
    this.branch = branch;
    this.commit = commit;
  }

  @Nonnull
  public static Head of(@Nonnull GitFileSystem gfs) {
    GfsStatusProvider status = gfs.getStatusProvider();
    String branch = status.isAttached() ? status.branch() : null;
    CommitView commit = status.isInitialized() ? CommitView.of(status.commit()) : null;
    return new Head(branch, commit);
  }

  @Nullable
  public String getBranch() {
    return branch;
  }

  @Nullable
  public CommitView getCommit() {
    return commit;
  }

}
