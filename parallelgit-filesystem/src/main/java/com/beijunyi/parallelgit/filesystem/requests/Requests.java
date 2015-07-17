package com.beijunyi.parallelgit.filesystem.requests;

import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;

public final class Requests {

  @Nonnull
  public static CommitRequest commit() {
    return CommitRequest.prepare();
  }

  @Nonnull
  public static CommitRequest commit(@Nonnull GitFileSystem gfs) {
    return CommitRequest.prepare(gfs);
  }

}
