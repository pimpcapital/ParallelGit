package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsObjectService;

public abstract class GfsChange {

  public void applyTo(@Nonnull DirectoryNode dir, @Nonnull String name) throws IOException {
    Node currentNode = ignoresCurrentNode() ? null : dir.getChild(name);

    if(shouldDelete(currentNode)) {
      dir.removeChild(name);
      return;
    }

    Node newNode = createNode(currentNode, dir.getObjService());
    if(newNode != null)
      dir.addChild(name, newNode, true);
  }

  protected abstract boolean ignoresCurrentNode();

  protected abstract boolean shouldDelete(@Nullable Node currentNode);

  @Nullable
  protected abstract Node createNode(@Nullable Node currentNode, @Nonnull GfsObjectService objService);

}
