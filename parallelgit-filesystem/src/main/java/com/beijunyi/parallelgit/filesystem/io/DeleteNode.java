package com.beijunyi.parallelgit.filesystem.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DeleteNode extends GfsChange {

  @Override
  protected boolean ignoresCurrentNode() {
    return true;
  }

  @Override
  protected boolean shouldDelete(@Nullable Node currentNode) {
    return true;
  }

  @Nullable
  @Override
  protected Node createNode(@Nullable Node currentNode, @Nonnull DirectoryNode parent) {
    throw new IllegalStateException();
  }

}
