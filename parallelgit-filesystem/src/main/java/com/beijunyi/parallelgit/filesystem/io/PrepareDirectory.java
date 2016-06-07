package com.beijunyi.parallelgit.filesystem.io;

import javax.annotation.Nullable;

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
  protected Node createNode(@Nullable Node currentNode, DirectoryNode parent) {
    if(currentNode == null || !currentNode.isDirectory())
      return DirectoryNode.newDirectory(parent);
    return null;
  }
}
