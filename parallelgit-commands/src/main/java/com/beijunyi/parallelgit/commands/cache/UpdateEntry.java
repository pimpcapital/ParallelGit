package com.beijunyi.parallelgit.commands.cache;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.CacheUtils;
import com.beijunyi.parallelgit.utils.io.CacheEntryUpdate;
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
    CacheEntryUpdate update = new CacheEntryUpdate(path);
    if(blobId != null)
      update.setNewBlob(blobId);
    if(mode != null)
      update.setNewFileMode(mode);
    CacheUtils.updateFile(update, provider.getEditor());
  }
}