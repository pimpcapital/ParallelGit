package com.beijunyi.parallelgit.runtime.cache;

import java.io.IOException;
import javax.annotation.Nonnull;

import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

public class UpdateEntry extends CacheEditor {

  private AnyObjectId blobId;
  private FileMode mode;

  public UpdateEntry(@Nonnull String path) {
    super(path);
  }

  public void setBlobId(@Nonnull AnyObjectId blobId) {
    this.blobId = blobId;
  }

  public void setMode(@Nonnull FileMode mode) {
    this.mode = mode;
  }

  @Override
  public void edit(@Nonnull CacheStateProvider provider) throws IOException {
    DirCache cache = provider.getCurrentCache();
    DirCacheEntry entry = cache.getEntry(path);
    if(entry == null)
      throw new IllegalArgumentException("entry not found: " + path);
    if(blobId != null)
      entry.setObjectId(blobId);
    if(mode != null)
      entry.setFileMode(mode);
  }
}