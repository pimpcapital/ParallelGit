package com.beijunyi.parallelgit.filesystem.merge;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.diff.Sequence;
import org.eclipse.jgit.merge.MergeResult;
import org.eclipse.jgit.revwalk.RevCommit;

public class GfsMergeNote {

  private final RevCommit source;
  private final String message;
  private final Map<String, MergeResult<? extends Sequence>> conflicts;

  private GfsMergeNote(@Nullable RevCommit source, String message, @Nullable Map<String, MergeResult<? extends Sequence>> conflicts) {
    this.source = source;
    this.message = message;
    this.conflicts = conflicts;
  }

  @Nonnull
  public static GfsMergeNote mergeSquash(String message) {
    return new GfsMergeNote(null, message, null);
  }

  @Nonnull
  public static GfsMergeNote mergeNoCommit(RevCommit source, String message) {
    return new GfsMergeNote(source, message, null);
  }

  @Nonnull
  public static GfsMergeNote mergeSquashConflicting(String message, @Nullable Map<String, MergeResult<? extends Sequence>> conflicts) {
    return new GfsMergeNote(null, message, conflicts);
  }

  @Nonnull
  public static GfsMergeNote mergeConflicting(RevCommit source, String message, @Nullable Map<String, MergeResult<? extends Sequence>> conflicts) {
    return new GfsMergeNote(source, message, conflicts);
  }

  @Nullable
  public RevCommit getSource() {
    return source;
  }

  @Nonnull
  public String getMessage() {
    return message;
  }

  @Nullable
  public Map<String, MergeResult<? extends Sequence>> getConflicts() {
    return conflicts;
  }

}
