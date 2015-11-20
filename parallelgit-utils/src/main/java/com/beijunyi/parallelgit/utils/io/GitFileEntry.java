package com.beijunyi.parallelgit.utils.io;

import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

public class GitFileEntry {

  private final AnyObjectId id;
  private final FileMode mode;

  public GitFileEntry(@Nonnull AnyObjectId id, @Nonnull FileMode mode) {
    this.id = id;
    this.mode = mode;
  }

  @Nonnull
  public AnyObjectId getId() {
    return id;
  }

  @Nonnull
  public FileMode getMode() {
    return mode;
  }
}
