package com.beijunyi.parallelgit.web.protocol.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;

public class Head {

  private final String branch;
  private final String commit;

  private Head(@Nullable String branch, @Nullable String commit) {
    this.branch = branch;
    this.commit = commit;
  }

  @Nonnull
  public static Head of(@Nonnull GitFileSystem gfs) {
    GfsStatusProvider status = gfs.getStatusProvider();
    String branch = status.isAttached() ? status.branch() : null;
    String commit = status.isInitialized() ? status.commit().getName() : null;
    return new Head(branch, commit);
  }

  @Nullable
  public String getBranch() {
    return branch;
  }

  @Nullable
  public String getCommit() {
    return commit;
  }

}
