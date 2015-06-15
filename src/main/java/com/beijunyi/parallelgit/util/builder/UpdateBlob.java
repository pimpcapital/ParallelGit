package com.beijunyi.parallelgit.util.builder;

import java.io.IOException;
import javax.annotation.Nonnull;

import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

class UpdateBlob extends CacheEditor {
  private AnyObjectId blobId;
  private FileMode mode;

  UpdateBlob(@Nonnull String path) {
    super(path);
  }

  void setBlobId(@Nonnull AnyObjectId blobId) {
    this.blobId = blobId;
  }

  void setMode(@Nonnull FileMode mode) {
    this.mode = mode;
  }

  @Override
  protected void doEdit(@Nonnull BuildStateProvider provider) throws IOException {
    DirCache cache = provider.getCurrentCache();
    DirCacheEntry entry = cache.getEntry(path);
    if(entry == null)
      throw new IllegalArgumentException("blob not found: " + path);
    if(blobId != null)
      entry.setObjectId(blobId);
    if(mode != null)
      entry.setFileMode(mode);
  }
}