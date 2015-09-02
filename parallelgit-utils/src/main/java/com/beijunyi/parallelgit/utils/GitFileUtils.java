package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;

public final class GitFileUtils {

  @Nullable
  public static byte[] readFileFromCommit(@Nonnull String file, @Nonnull AnyObjectId commit, @Nonnull ObjectReader reader) throws IOException {
    AnyObjectId blobId = ObjectUtils.findObject(file, commit, reader);
    if(blobId == null)
      return null;
    return ObjectUtils.readObject(blobId, reader);
  }

  @Nullable
  public static byte[] readFileFromCommit(@Nonnull String file, @Nonnull AnyObjectId commit, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return readFileFromCommit(file, commit, reader);
    }
  }

  @Nullable
  public static byte[] readFileFromCommit(@Nonnull String file, @Nonnull String commit, @Nonnull Repository repo) throws IOException {
    return readFileFromCommit(file, repo.resolve(commit), repo);
  }

}
