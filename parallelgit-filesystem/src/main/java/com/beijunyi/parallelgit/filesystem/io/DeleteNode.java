package com.beijunyi.parallelgit.filesystem.io;

import javax.annotation.Nullable;

public class DeleteNode extends GfsChange {

  @Nullable
  @Override
  protected Node convertNode(@Nullable Node node, DirectoryNode parent) {
    return null;
  }

}
