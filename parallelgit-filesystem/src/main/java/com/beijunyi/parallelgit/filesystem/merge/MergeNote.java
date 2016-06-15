package com.beijunyi.parallelgit.filesystem.merge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.revwalk.RevCommit;

public class MergeNote {

  private final RevCommit source;
  private final String message;

  private MergeNote(@Nullable RevCommit source, String message) {
    this.source = source;
    this.message = message;
  }

  @Nonnull
  public static MergeNote mergeSquash(String message) {
    return new MergeNote(null, message);
  }

  @Nonnull
  public static MergeNote mergeNoCommit(RevCommit source, String message) {
    return new MergeNote(source, message);
  }

  @Nonnull
  public static MergeNote mergeSquashConflicting(String message) {
    return new MergeNote(null, message);
  }

  @Nonnull
  public static MergeNote mergeConflicting(RevCommit source, String message) {
    return new MergeNote(source, message);
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
