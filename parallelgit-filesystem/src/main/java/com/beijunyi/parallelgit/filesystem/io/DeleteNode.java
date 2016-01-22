package com.beijunyi.parallelgit.filesystem.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsObjectService;

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
  protected Node createNode(@Nullable Node currentNode, @Nonnull GfsObjectService objService) {
    throw new IllegalStateException();
  }

}
