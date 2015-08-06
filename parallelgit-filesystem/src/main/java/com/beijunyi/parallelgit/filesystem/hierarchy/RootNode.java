package com.beijunyi.parallelgit.filesystem.hierarchy;

import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;

public class RootNode extends DirectoryNode {

  private RootNode(@Nonnull AnyObjectId object) {
    super(object);
  }

  private RootNode() {
    this(ObjectId.zeroId());
    children = new HashMap<>();
    dirty = true;
  }

  @Nonnull
  public static RootNode newRoot(@Nullable AnyObjectId treeId) {
    return treeId != null ? new RootNode(treeId) : new RootNode();
  }

}
