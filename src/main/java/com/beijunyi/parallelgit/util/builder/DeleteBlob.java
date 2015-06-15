package com.beijunyi.parallelgit.util.builder;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.util.DirCacheHelper;

class DeleteBlob extends CacheEditor {
  DeleteBlob(@Nonnull String path) {
    super(path);
  }

  @Override
  protected void doEdit(@Nonnull BuildStateProvider provider) throws IOException {
    DirCacheHelper.deleteFile(provider.getEditor(), path);
  }
}