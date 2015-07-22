package com.beijunyi.parallelgit.filesystem.hierarchy;

public enum TreeNodeType {
  NON_EXECUTABLE_FILE(true),
  EXECUTABLE_FILE(true),
  SYMBOLIC_LINK(false),
  DIRECTORY(false);

  private final boolean regularFile;

  TreeNodeType(boolean regularFile) {
    this.regularFile = regularFile;
  }

  public boolean isRegularFile() {
    return regularFile;
  }
}
