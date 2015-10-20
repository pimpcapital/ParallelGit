package com.beijunyi.parallelgit.utils.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.TreeUtils;
import org.eclipse.jgit.dircache.DirCacheEditor;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

public class CacheEntryUpdate extends DirCacheEditor.PathEdit {

  private AnyObjectId newBlob;
  private FileMode newFileMode;

  public CacheEntryUpdate(@Nonnull String entryPath) {
    super(TreeUtils.normalizeTreePath(entryPath));
  }

  @Nonnull
  public CacheEntryUpdate setNewBlob(@Nullable AnyObjectId blob) {
    this.newBlob = blob;
    return this;
  }

  @Nonnull
  public CacheEntryUpdate setNewFileMode(@Nullable FileMode fileMode) {
    this.newFileMode = fileMode;
    return this;
  }

  @Override
  public void apply(@Nonnull DirCacheEntry ent) {
    if(newBlob != null)
      ent.setObjectId(newBlob);
    if(newFileMode != null)
      ent.setFileMode(newFileMode);
  }

}
