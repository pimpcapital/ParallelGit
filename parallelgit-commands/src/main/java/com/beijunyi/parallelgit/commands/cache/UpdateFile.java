package com.beijunyi.parallelgit.commands.cache;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.exceptions.NoSuchCacheEntryException;
import com.beijunyi.parallelgit.utils.io.CacheEntryUpdate;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;

public class UpdateFile extends CacheFileEditor {

  private boolean create;

  public UpdateFile(@Nonnull String path) {
    super(path);
  }

  public void setCreate(boolean create) {
    this.create = create;
  }

  private void updateEntry(@Nonnull DirCacheEntry entry, @Nonnull CacheStateProvider provider) throws IOException {
    AnyObjectId blobId = provider.getInserter().insert(Constants.OBJ_BLOB, bytes);
    CacheEntryUpdate update = new CacheEntryUpdate()
    if(mode != null)
      entry.setFileMode(mode);
    entry.setObjectId(blobId);
  }

  @Override
  public void edit(@Nonnull CacheStateProvider provider) throws IOException {
    DirCache cache = provider.getCurrentCache();
    DirCacheEntry entry = cache.getEntry(path);
    if(entry == null && !create)
      throw new NoSuchCacheEntryException(path);
    prepareFileMode();
    prepareBytes();
    if(entry == null)
      createEntry(provider);
    else
      updateEntry(entry, provider);
  }

}
