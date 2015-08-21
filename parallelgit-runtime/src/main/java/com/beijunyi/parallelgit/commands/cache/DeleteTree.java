package com.beijunyi.parallelgit.commands.cache;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.CacheHelper;

public class DeleteTree extends CacheEditor {

  public DeleteTree(@Nonnull String path) {
    super(path);
  }

  @Override
  public void edit(@Nonnull CacheStateProvider provider) throws IOException {
    CacheHelper.deleteDirectory(provider.getEditor(), path);
  }
}