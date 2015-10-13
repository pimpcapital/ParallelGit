package com.beijunyi.parallelgit.commands.cache;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.CacheUtils;

public class DeleteEntry extends CacheEditor {
  public DeleteEntry(@Nonnull String path) {
    super(path);
  }

  @Override
  public void edit(@Nonnull CacheStateProvider provider) throws IOException {
    CacheUtils.deleteFile(path, provider.getEditor());
  }
}