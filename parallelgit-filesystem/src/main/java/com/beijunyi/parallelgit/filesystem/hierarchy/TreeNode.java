package com.beijunyi.parallelgit.filesystem.hierarchy;

import org.eclipse.jgit.lib.ObjectId;

public abstract class TreeNode {

  private TreeNodeType type;
  private ObjectId object;
  private boolean dirty;
  private TreeNode parent;

}
