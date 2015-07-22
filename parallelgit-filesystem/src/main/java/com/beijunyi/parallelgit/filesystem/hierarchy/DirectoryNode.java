package com.beijunyi.parallelgit.filesystem.hierarchy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DirectoryNode extends TreeNode {

  private final Map<String, TreeNode> children = new ConcurrentHashMap<>();
  private DirectoryNode parent;

}
