package com.beijunyi.parallelgit.runtime.cache;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.CacheUtils;

public class DeleteTree extends CacheEditor {

  public DeleteTree(@Nonnull String path) {
    super(path);
  }

  @Override
  public void edit(@Nonnull CacheStateProvider provider) throws IOException {
    CacheUtils.deleteDirectory(path, provider.getEditor());
  }
}