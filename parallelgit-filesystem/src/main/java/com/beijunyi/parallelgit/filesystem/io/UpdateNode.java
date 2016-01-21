package com.beijunyi.parallelgit.filesystem.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsObjectService;
import com.beijunyi.parallelgit.utils.io.GitFileEntry;

public class UpdateNode extends GfsChange {

  private final GitFileEntry entry;

  public UpdateNode(@Nonnull GitFileEntry entry) {
    this.entry = entry;
  }

  @Override
  protected boolean isDeletion() {
    return false;
  }

  @Override
  protected boolean ignoresCurrentNode() {
    return true;
  }

  @Nonnull
  @Override
  protected Node createNode(@Nullable Node currentNode, @Nonnull GfsObjectService objService) {
    return Node.fromEntry(entry, objService);
  }
}
