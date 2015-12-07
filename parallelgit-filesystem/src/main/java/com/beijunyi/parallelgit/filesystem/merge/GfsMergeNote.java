package com.beijunyi.parallelgit.filesystem.merge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.revwalk.RevCommit;

public class GfsMergeNote {

  private final RevCommit source;
  private final String message;

  private GfsMergeNote(@Nullable RevCommit source, @Nonnull String message) {
    this.source = source;
    this.message = message;
  }

  @Nonnull
  public static GfsMergeNote mergeSquash(@Nonnull String message) {
    return new GfsMergeNote(null, message);
  }

  @Nonnull
  public static GfsMergeNote mergeNoCommit(@Nonnull RevCommit source, @Nonnull String message) {
    return new GfsMergeNote(source, message);
  }

  @Nullable
  public RevCommit getSource() {
    return source;
  }

  @Nonnull
  public String getMessage() {
    return message;
  }

}
