package com.beijunyi.parallelgit.filesystem.requests;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import org.eclipse.jgit.lib.AnyObjectId;

public final class PersistRequest extends GitFileSystemRequest<PersistRequest, AnyObjectId> {

  @Nonnull
  public static PersistRequest prepare() {
    return new PersistRequest();
  }

  @Nonnull
  public static PersistRequest prepare(@Nonnull GitFileSystem gfs) {
    return prepare().gfs(gfs);
  }

  @Nonnull
  @Override
  protected PersistRequest self() {
    return this;
  }

  @Nullable
  @Override
  protected AnyObjectId doExecute() throws IOException {
    return gfs.getFileStore().persistChanges();
  }
}
