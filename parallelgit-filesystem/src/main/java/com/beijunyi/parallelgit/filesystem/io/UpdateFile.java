package com.beijunyi.parallelgit.filesystem.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsObjectService;
import org.eclipse.jgit.lib.FileMode;

public class UpdateFile extends GfsChange {

  private final byte[] bytes;
  private final FileMode mode;

  public UpdateFile(@Nonnull byte[] bytes, @Nonnull FileMode mode) {
    this.bytes = bytes;
    this.mode = mode;
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
    return FileNode.fromBytes(bytes, mode, objService);
  }
}
