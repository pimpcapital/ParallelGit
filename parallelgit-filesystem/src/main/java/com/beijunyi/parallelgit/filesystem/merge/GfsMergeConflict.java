package com.beijunyi.parallelgit.filesystem.merge;

import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

public class GfsMergeConflict {

  private final FileMode baseMode;
  private final AnyObjectId baseId;

  private final FileMode ourMode;
  private final AnyObjectId ourId;

  private final FileMode theirMode;
  private final AnyObjectId theirId;


  public GfsMergeConflict(@Nonnull FileMode baseMode, @Nonnull AnyObjectId baseId, @Nonnull FileMode ourMode, @Nonnull AnyObjectId ourId, @Nonnull FileMode theirMode, @Nonnull AnyObjectId theirId) {
    this.baseMode = baseMode;
    this.baseId = baseId;
    this.ourMode = ourMode;
    this.ourId = ourId;
    this.theirMode = theirMode;
    this.theirId = theirId;
  }


}
