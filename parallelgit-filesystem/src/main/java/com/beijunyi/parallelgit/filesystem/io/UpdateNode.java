package com.beijunyi.parallelgit.filesystem.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.io.GitFileEntry;

public class UpdateNode extends GfsChange {

  private final GitFileEntry entry;

  public UpdateNode(@Nonnull GitFileEntry entry) {
    this.entry = entry;
  }

  @Override
  protected boolean ignoresCurrentNode() {
    return true;
  }

  @Override
  protected boolean shouldDelete(@Nullable Node currentNode) {
    return false;
  }

  @Nonnull
  @Override
  protected Node createNode(@Nullable Node currentNode, DirectoryNode parent) {
    return Node.fromEntry(entry, parent);
  }
}
