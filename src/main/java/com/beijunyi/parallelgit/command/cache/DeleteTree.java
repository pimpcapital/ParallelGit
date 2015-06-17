package com.beijunyi.parallelgit.command.cache;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.util.DirCacheHelper;

public class DeleteTree extends CacheEditor {

  public DeleteTree(@Nonnull String path) {
    super(path);
  }

  @Override
  public void edit(@Nonnull CacheStateProvider provider) throws IOException {
    DirCacheHelper.deleteDirectory(provider.getEditor(), path);
  }
}