package com.beijunyi.parallelgit.runtime.cache;

import java.io.IOException;
import javax.annotation.Nonnull;

public class AddFile extends CacheFileEditor {

  public AddFile(@Nonnull String path) {
    super(path);
  }

  @Override
  public void edit(@Nonnull CacheStateProvider provider) throws IOException {
    prepareFileMode();
    prepareBytes();
    createEntry(provider);
  }

}
