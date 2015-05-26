package com.beijunyi.parallelgit.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class VirtualDirCacheEntry {

  private final String path;
  private final boolean regularFile;

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

  @Override
  public boolean equals(@Nullable Object obj) {
    if(this == obj)
      return true;
    if(obj == null || getClass() != obj.getClass())
      return false;

    VirtualDirCacheEntry that = (VirtualDirCacheEntry)obj;

    return regularFile == that.regularFile && path.equals(that.path);

  }

  @Override
  public int hashCode() {
    int result = path.hashCode();
    result = 31 * result + (regularFile ? 1 : 0);
    return result;
  }
}
