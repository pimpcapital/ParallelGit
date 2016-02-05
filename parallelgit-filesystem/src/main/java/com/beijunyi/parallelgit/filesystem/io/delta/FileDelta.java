package com.beijunyi.parallelgit.filesystem.io.delta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.io.GitFileEntry;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

public class FileDelta {

  private final DeltaType type;
  private final GitFileEntry origin;
  private final GitFileEntry current;

  public FileDelta(@Nonnull DeltaType type, @Nullable GitFileEntry origin, @Nullable GitFileEntry current) {
    this.type = type;
    this.origin = origin;
    this.current = current;
  }

  @Nonnull
  public DeltaType getType() {
    return type;
  }

  @Nullable
  public GitFileEntry getOrigin() {
    return origin;
  }

  @Nullable
  public GitFileEntry getCurrent() {
    return current;
  }

  public boolean isDirectory() {
    return (origin != null && origin.isDirectory()) || (current != null && current.isDirectory());
  }

  @Nonnull
  public DirectoryDelta asDirectory() {
    return (DirectoryDelta) this;
  }

}
