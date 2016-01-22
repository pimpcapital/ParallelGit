package com.beijunyi.parallelgit.filesystem.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsObjectService;

import static org.eclipse.jgit.lib.FileMode.REGULAR_FILE;

public class UpdateFile extends GfsChange {

  private final byte[] bytes;

  public UpdateFile(@Nonnull byte[] bytes) {
    this.bytes = bytes;
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
  protected Node createNode(@Nullable Node currentNode, @Nonnull GfsObjectService objService) {
    return FileNode.fromBytes(bytes, REGULAR_FILE, objService);
  }
}
