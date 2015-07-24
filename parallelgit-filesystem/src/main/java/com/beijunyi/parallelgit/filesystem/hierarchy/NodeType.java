package com.beijunyi.parallelgit.filesystem.hierarchy;

public enum NodeType {
  NON_EXECUTABLE_FILE(true),
  EXECUTABLE_FILE(true),
  SYMBOLIC_LINK(false),
  DIRECTORY(false);

  private final boolean regularFile;

  NodeType(boolean regularFile) {
    this.regularFile = regularFile;
  }

  public boolean isRegularFile() {
    return regularFile;
  }
}
