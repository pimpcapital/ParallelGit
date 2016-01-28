package com.beijunyi.parallelgit.filesystem.merge;

import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import org.eclipse.jgit.merge.ResolveMerger;

public class GfsConflictCollector {

  private final GitFileSystem gfs;

  public GfsConflictCollector(@Nonnull  GitFileSystem gfs) {
    this.gfs = gfs;
  }

  public void collectFrom(@Nonnull ResolveMerger merger) {

  }

}
