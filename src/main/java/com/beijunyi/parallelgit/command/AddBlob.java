package com.beijunyi.parallelgit.command;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.util.DirCacheHelper;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

class AddBlob extends CacheEditor {
  private AnyObjectId blobId;
  private FileMode mode;

  AddBlob(@Nonnull String path) {
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
    DirCacheHelper.addFile(provider.getCurrentBuilder(), mode, path, blobId);
  }
}