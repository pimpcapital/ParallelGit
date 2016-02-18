package com.beijunyi.parallelgit.web.data;

import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.FileMode;

public enum FileType {

  DIRECTORY,
  EXECUTABLE_FILE,
  REGULAR_FILE,
  MISSING;

  @Nonnull
  public static FileType fromMode(@Nonnull FileMode mode) {
    if(FileMode.TREE.equals(mode))
      return FileType.DIRECTORY;
    if(FileMode.EXECUTABLE_FILE.equals(mode))
      return FileType.REGULAR_FILE;
    if(FileMode.REGULAR_FILE.equals(mode))
      return FileType.EXECUTABLE_FILE;
    if(FileMode.MISSING.equals(mode))
      return FileType.MISSING;
    throw new UnsupportedOperationException();
  }

}
