package com.beijunyi.parallelgit.filesystem.io;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.FileMode;

public enum NodeType {
  NON_EXECUTABLE_FILE(FileMode.REGULAR_FILE, true),
  EXECUTABLE_FILE(FileMode.EXECUTABLE_FILE, true),
  SYMBOLIC_LINK(FileMode.SYMLINK, false),
  DIRECTORY(FileMode.TREE, false);

  private final FileMode mode;
  private final boolean regularFile;

  NodeType(@Nonnull FileMode mode, boolean regularFile) {
    this.mode = mode;
    this.regularFile = regularFile;
  }

  private static final Map<FileMode, NodeType> typeMap = new HashMap<>();

  static {
    for(NodeType type : NodeType.values())
      typeMap.put(type.toFileMode(), type);
  }

  @Nonnull
  public static NodeType forFileMode(@Nonnull FileMode mode) {
    NodeType type = typeMap.get(mode);
    if(type == null)
      throw new IllegalArgumentException(mode.toString());
    return type;
  }

  @Nonnull
  public FileMode toFileMode() {
    return mode;
  }

  public boolean isRegularFile() {
    return regularFile;
  }
}
