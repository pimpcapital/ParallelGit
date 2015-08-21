package com.beijunyi.parallelgit.runtime.cache;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.CacheHelper;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

public class AddBlob extends CacheEditor {
  private AnyObjectId blobId;
  private FileMode mode;

  public AddBlob(@Nonnull String path) {
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
    CacheHelper.addFile(provider.getCurrentBuilder(), mode, path, blobId);
  }
}