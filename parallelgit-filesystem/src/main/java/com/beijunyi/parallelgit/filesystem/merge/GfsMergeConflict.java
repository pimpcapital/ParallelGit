package com.beijunyi.parallelgit.filesystem.merge;

import javax.annotation.Nonnull;

public class GfsMergeConflict {

  private final ThreeWayEntry entry;


  public GfsMergeConflict(@Nonnull ThreeWayEntry entry) {
    this.entry = entry;
  }


}
