package com.beijunyi.parallelgit.runtime.cache;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.CacheHelper;

public class DeleteEntry extends CacheEditor {
  public DeleteEntry(@Nonnull String path) {
    super(path);
  }

  @Override
  public void edit(@Nonnull CacheStateProvider provider) throws IOException {
    CacheHelper.deleteFile(provider.getEditor(), path);
  }
}