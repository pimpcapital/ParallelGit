package com.beijunyi.parallelgit.filesystem.io;

import javax.annotation.Nullable;

import static com.beijunyi.parallelgit.filesystem.io.DirectoryNode.newDirectory;

public class MakeDirectory extends GfsChange {

  @Nullable
  @Override
  protected Node convertNode(@Nullable Node node, DirectoryNode parent) {
    if(node == null || !node.isDirectory())
      return newDirectory(parent);
    return node;
  }

}
