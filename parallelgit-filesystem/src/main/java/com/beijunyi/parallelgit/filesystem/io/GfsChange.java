package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import javax.annotation.Nullable;

public abstract class GfsChange {

  public void applyTo(DirectoryNode dir, String name) throws IOException {
    Node currentNode = dir.getChild(name);
    Node newNode = convertNode(currentNode, dir);
    if(newNode == null) {
      dir.removeChild(name);
    } else {
      dir.addChild(name, newNode, true);
    }
  }

  @Nullable
  protected abstract Node convertNode(@Nullable Node node, DirectoryNode parent);

}
