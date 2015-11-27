package com.beijunyi.parallelgit.utils.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

  @Override
  public boolean equals(@Nullable Object obj) {
    if(this == obj) return true;
    if(obj == null || getClass() != obj.getClass()) return false;
    GitFileEntry that = (GitFileEntry)obj;
    return id.equals(that.id) && mode.equals(that.mode);

  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + mode.hashCode();
    return result;
  }
}
