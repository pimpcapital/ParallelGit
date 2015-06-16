package com.beijunyi.parallelgit.command;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.util.DirCacheHelper;

class DeleteTree extends CacheEditor {

  DeleteTree(@Nonnull String path) {
    super(path);
  }

  @Override
  protected void doEdit(@Nonnull BuildStateProvider provider) throws IOException {
    DirCacheHelper.deleteDirectory(provider.getEditor(), path);
  }
}