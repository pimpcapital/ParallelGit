package com.beijunyi.parallelgit.filesystem.io.delta;

import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.io.GitFileEntry;

public class DirectoryDelta extends FileDelta {

  private final SortedMap<String, DeltaType> children;

  public DirectoryDelta(@Nonnull DeltaType type, @Nullable GitFileEntry origin, @Nullable GitFileEntry current, @Nonnull SortedMap<String, DeltaType> children) {
    super(type, origin, current);
    this.children = children;
  }

  @Nonnull
  public SortedMap<String, DeltaType> getChildren() {
    return children;
  }
}
