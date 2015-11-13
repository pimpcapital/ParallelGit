package com.beijunyi.parallelgit.filesystem;

import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.requests.CommitRequest;
import com.beijunyi.parallelgit.filesystem.requests.PersistRequest;
import com.beijunyi.parallelgit.filesystem.utils.GitFileSystemBuilder;

public final class Gfs {

  @Nonnull
  public static GitFileSystemBuilder newFileSystem() {
    return GitFileSystemBuilder.prepare();
  }

  @Nonnull
  public static CommitRequest commit(@Nonnull GitFileSystem gfs) {
    return CommitRequest.prepare(gfs);
  }

  @Nonnull
  public static PersistRequest persist(@Nonnull GitFileSystem gfs) {
    return PersistRequest.prepare(gfs);
  }

}
