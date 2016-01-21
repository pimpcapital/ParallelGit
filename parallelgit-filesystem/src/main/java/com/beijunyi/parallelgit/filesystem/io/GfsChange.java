package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsObjectService;

public abstract class GfsChange {

  public void applyTo(@Nonnull DirectoryNode dir, @Nonnull String name) throws IOException {
    if(isDeletion())
      dir.removeChild(name);

    Node currentNode = ignoresCurrentNode() ? null : dir.getChild(name);
    Node newNode = createNode(currentNode, dir.getObjService());
    if(newNode != null)
      dir.addChild(name, DirectoryNode.newDirectory(dir.getObjService()), true);
  }

  protected abstract boolean isDeletion();

  protected abstract boolean ignoresCurrentNode();

  @Nullable
  protected abstract Node createNode(@Nullable Node currentNode, @Nonnull GfsObjectService objService);

}
