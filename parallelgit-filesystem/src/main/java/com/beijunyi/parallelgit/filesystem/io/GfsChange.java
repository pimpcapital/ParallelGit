package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import javax.annotation.Nullable;

public abstract class GfsChange {

  public void applyTo(DirectoryNode dir, String name) throws IOException {
    Node currentNode = ignoresCurrentNode() ? null : dir.getChild(name);

    if(shouldDelete(currentNode)) {
      dir.removeChild(name);
      return;
    }

    Node newNode = createNode(currentNode, dir);
    if(newNode != null)
      dir.addChild(name, newNode, true);
  }

  protected abstract boolean ignoresCurrentNode();

  protected abstract boolean shouldDelete(@Nullable Node currentNode);

  @Nullable
  protected abstract Node createNode(@Nullable Node currentNode, DirectoryNode parent);

}
