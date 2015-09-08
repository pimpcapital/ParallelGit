package com.beijunyi.parallelgit.utils;

import javax.annotation.Nonnull;

public final class VirtualDirCacheEntry {

  private final String path;
  private final boolean regularFile;
  private String childrenPrefix;

  private String name;

  private VirtualDirCacheEntry(@Nonnull String path, boolean regularFile) {
    this.path = path;
    this.regularFile = regularFile;
  }

  @Nonnull
  static VirtualDirCacheEntry file(@Nonnull String pathStr) {
    return new VirtualDirCacheEntry(pathStr, true);
  }

  static VirtualDirCacheEntry directory(@Nonnull String pathStr) {
    return new VirtualDirCacheEntry(pathStr, false);
  }

  @Nonnull
  public String getPath() {
    return path;
  }

  @Nonnull
  public String getName() {
    if(name == null) {
      int start = path.lastIndexOf('/');
      name = path.substring(start + 1);
    }
    return name;
  }

  public boolean isRegularFile() {
    return regularFile;
  }

  public boolean isDirectory() {
    return !regularFile;
  }

  public boolean hasChild(@Nonnull String path) {
    if(regularFile)
      return false;
    if(childrenPrefix == null)
      childrenPrefix = this.path + "/";
    return path.startsWith(childrenPrefix);
  }

}
