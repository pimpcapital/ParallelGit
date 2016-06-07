package com.beijunyi.parallelgit.utils.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.exceptions.NoSuchCacheEntryException;
import org.eclipse.jgit.dircache.DirCacheEntry;

public final class CacheNode {

  private final String path;
  private final DirCacheEntry entry;
  private String childrenPrefix;

  private CacheNode(String path, @Nullable DirCacheEntry entry) {
    this.path = path;
    this.entry = entry;
  }

  @Nonnull
  public static CacheNode file(String pathStr, DirCacheEntry entry) {
    return new CacheNode(pathStr, entry);
  }

  @Nonnull
  public static CacheNode directory(String pathStr) {
    return new CacheNode(pathStr, null);
  }

  @Nonnull
  public String getPath() {
    return path;
  }

  public boolean isFile() {
    return entry != null;
  }

  public boolean isDirectory() {
    return entry == null;
  }

  @Nonnull
  public DirCacheEntry getEntry() {
    if(entry == null)
      throw new NoSuchCacheEntryException(path);
    return entry;
  }

  boolean hasChild(String childPath) {
    return isDirectory() && childPath.startsWith(getChildrenPrefix());
  }

  @Nonnull
  private String getChildrenPrefix() {
    if(childrenPrefix == null)
      childrenPrefix = path + "/";
    return childrenPrefix;
  }

}
