package com.beijunyi.parallelgit.filesystem.merge;

import javax.annotation.Nonnull;

import org.eclipse.jgit.revwalk.RevCommit;

public class GfsMergeNote {

  private final RevCommit source;
  private final String message;

  public GfsMergeNote(@Nonnull RevCommit source, @Nonnull String message) {
    this.source = source;
    this.message = message;
  }

  @Nonnull
  public RevCommit getSource() {
    return source;
  }

  @Nonnull
  public String getMessage() {
    return message;
  }

}
