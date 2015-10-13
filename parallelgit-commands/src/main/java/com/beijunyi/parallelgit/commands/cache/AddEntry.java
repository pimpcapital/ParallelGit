package com.beijunyi.parallelgit.commands.cache;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.CacheUtils;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

public class AddEntry extends CacheEditor {
  private AnyObjectId blobId;
  private FileMode mode;

  public AddEntry(@Nonnull String path) {
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
    CacheUtils.addFile(path, mode, blobId, provider.getCurrentBuilder());
  }
}