package com.beijunyi.parallelgit.commands.cache;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.DirCacheHelper;

public class DeleteBlob extends CacheEditor {
  public DeleteBlob(@Nonnull String path) {
    super(path);
  }

  @Override
  public void edit(@Nonnull CacheStateProvider provider) throws IOException {
    DirCacheHelper.deleteFile(provider.getEditor(), path);
  }
}