package com.beijunyi.parallelgit.web.protocol.model;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import org.eclipse.jgit.lib.Repository;

import static com.beijunyi.parallelgit.utils.BranchUtils.*;
import static com.beijunyi.parallelgit.utils.RefUtils.ensureBranchRefName;

public class Head {

  private final String ref;
  private final CommitView commit;

  private Head(@Nullable String ref, @Nullable CommitView commit) {
    this.ref = ref;
    this.commit = commit;
  }

  @Nonnull
  public static Head of(@Nonnull GitFileSystem gfs) {
    GfsStatusProvider status = gfs.getStatusProvider();
    String ref = status.isAttached() ? ensureBranchRefName(status.branch()) : null;
    CommitView commit = status.isInitialized() ? CommitView.of(status.commit()) : null;
    return new Head(ref, commit);
  }

  @Nonnull
  public static Head of(@Nonnull String branch, @Nonnull Repository repo) throws IOException {
    String ref = ensureBranchRefName(branch);
    CommitView commit = branchExists(ref, repo) ? CommitView.of(getHeadCommit(ref, repo)) : null;
    return new Head(branch, commit);
  }

  @Nullable
  public String getRef() {
    return ref;
  }

  @Nullable
  public CommitView getCommit() {
    return commit;
  }

}
