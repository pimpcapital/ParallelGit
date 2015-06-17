package com.beijunyi.parallelgit.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class VirtualDirCacheEntry {

  private final String path;
  private final boolean regularFile;
  private String childrenPrefix;

  private String name;

  private VirtualDirCacheEntry(@Nonnull String path, boolean regularFile) {
    this.path = path;
    this.regularFile = regularFile;
  }

  /**
   * Creates a file {@code VirtualDirCacheEntry}.
   *
   * @param   pathStr
   *          the string path to the file
   * @return  a file {@code VirtualDirCacheEntry}
   */
  @Nonnull
  static VirtualDirCacheEntry file(@Nonnull String pathStr) {
    return new VirtualDirCacheEntry(pathStr, true);
  }

  /**
   * Creates a directory {@code VirtualDirCacheEntry}.
   *
   * @param   pathStr
   *          the string path to the directory
   * @return  a directory {@code VirtualDirCacheEntry}
   */
  static VirtualDirCacheEntry directory(@Nonnull String pathStr) {
    return new VirtualDirCacheEntry(pathStr, false);
  }

  /**
   * Returns the string path to this entry.
   *
   * @return  the string path to this entry
   */
  @Nonnull
  public String getPath() {
    return path;
  }

  /**
   * Returns the name of this entry.
   *
   * @return  the name of this entry
   */
  @Nonnull
  public String getName() {
    if(name == null) {
      int start = path.lastIndexOf('/');
      name = path.substring(start + 1);
    }
    return name;
  }

  /**
   * Tells if this entry is a regular file.
   *
   * @return  {@code true} if this entry is a regular file.
   */
  public boolean isRegularFile() {
    return regularFile;
  }

  /**
   * Tells if this entry is a directory.
   *
   * @return  {@code true} if this entry is a directory.
   */
  public boolean isDirectory() {
    return !regularFile;
  }

  /**
   * Tests if the given path is a child of this entry.
   *
   * @param   path
   *          the path to test
   * @return  {@code true} if the given path is a child of this entry.
   */
  public boolean hasChild(@Nonnull String path) {
    if(regularFile)
      return false;
    if(childrenPrefix == null)
      childrenPrefix = this.path + "/";
    return path.startsWith(childrenPrefix);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if(this == obj)
      return true;
    if(obj == null || getClass() != obj.getClass())
      return false;
    VirtualDirCacheEntry that = (VirtualDirCacheEntry)obj;
    return path.equals(that.path) && regularFile == that.regularFile;

  }

  @Override
  public int hashCode() {
    return 31 * path.hashCode() + (regularFile ? 1 : 0);
  }
}
