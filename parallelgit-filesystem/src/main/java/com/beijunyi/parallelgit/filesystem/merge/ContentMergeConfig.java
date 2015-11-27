package com.beijunyi.parallelgit.filesystem.merge;

import java.util.List;
import javax.annotation.Nonnull;

import org.eclipse.jgit.merge.MergeAlgorithm;
import org.eclipse.jgit.merge.MergeFormatter;

public class ContentMergeConfig {

  private final MergeAlgorithm algorithm;
  private final MergeFormatter formatter;
  private final List<String> conflictMarkers;

  public ContentMergeConfig(@Nonnull MergeAlgorithm algorithm, @Nonnull MergeFormatter formatter, @Nonnull List<String> conflictMarkers) {
    this.algorithm = algorithm;
    this.formatter = formatter;
    this.conflictMarkers = conflictMarkers;
  }

  @Nonnull
  public MergeAlgorithm getAlgorithm() {
    return algorithm;
  }

  @Nonnull
  public MergeFormatter getFormatter() {
    return formatter;
  }

  @Nonnull
  public List<String> getConflictMarkers() {
    return conflictMarkers;
  }

}
