package com.beijunyi.parallelgit.filesystems;

import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public abstract class GitDirectoryStream implements DirectoryStream<Path> {

  protected final String pathStr;
  protected final GitFileStore store;

  protected GitDirectoryStream(@Nonnull String pathStr, @Nonnull GitFileStore store) {
    this.pathStr = pathStr;
    this.store = store;
  }

  @Nonnull
  public String getPathStr() {
    return pathStr;
  }

  @Override
  public void close() {
    store.removeDirectoryStream(this);
  }
}
