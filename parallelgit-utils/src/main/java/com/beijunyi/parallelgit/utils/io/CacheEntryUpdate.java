package com.beijunyi.parallelgit.utils.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.TreeUtils;
import org.eclipse.jgit.dircache.DirCacheEditor;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

public class CacheEntryUpdate extends DirCacheEditor.PathEdit {

  private ObjectId newBlob;
  private FileMode newFileMode;

  public CacheEntryUpdate(String entryPath) {
    super(TreeUtils.normalizeNodePath(entryPath));
  }

  @Nonnull
  public CacheEntryUpdate setNewBlob(@Nullable ObjectId blob) {
    this.newBlob = blob;
    return this;
  }

  @Nonnull
  public CacheEntryUpdate setNewFileMode(@Nullable FileMode fileMode) {
    this.newFileMode = fileMode;
    return this;
  }

  @Override
  public void apply(DirCacheEntry ent) {
    if(newBlob != null)
      ent.setObjectId(newBlob);
    if(newFileMode != null)
      ent.setFileMode(newFileMode);
  }

}
