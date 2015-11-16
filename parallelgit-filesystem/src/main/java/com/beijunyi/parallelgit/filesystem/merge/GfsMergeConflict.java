package com.beijunyi.parallelgit.filesystem.merge;

import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.AnyObjectId;

public class GfsMergeConflict {

  private final int baseMode;
  private final AnyObjectId baseId;

  private final int ourMode;
  private final AnyObjectId ourId;

  private final int theirMode;
  private final AnyObjectId theirId;


  public GfsMergeConflict(int baseMode, @Nonnull AnyObjectId baseId, int ourMode, @Nonnull AnyObjectId ourId, int theirMode, @Nonnull AnyObjectId theirId) {
    this.baseMode = baseMode;
    this.baseId = baseId;
    this.ourMode = ourMode;
    this.ourId = ourId;
    this.theirMode = theirMode;
    this.theirId = theirId;
  }


}
