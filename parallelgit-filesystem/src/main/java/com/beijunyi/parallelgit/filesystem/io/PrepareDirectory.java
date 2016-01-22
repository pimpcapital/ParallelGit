package com.beijunyi.parallelgit.filesystem.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsObjectService;

public class PrepareDirectory extends GfsChange {

  @Override
  protected boolean ignoresCurrentNode() {
    return false;
  }

  @Override
  protected boolean shouldDelete(@Nullable Node currentNode) {
    return false;
  }

  @Nullable
  @Override
  protected Node createNode(@Nullable Node currentNode, @Nonnull GfsObjectService objService) {
    if(currentNode == null || !currentNode.isDirectory())
      return DirectoryNode.newDirectory(objService);
    return null;
  }
}
