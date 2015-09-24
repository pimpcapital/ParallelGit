package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;

public final class GitFileUtils {

  @Nonnull
  public static InputStream openFile(@Nonnull String file, @Nonnull AnyObjectId commit, @Nonnull ObjectReader reader) throws IOException {
    AnyObjectId blobId = ObjectUtils.findObject(file, commit, reader);
    if(blobId == null)
      throw new NoSuchFileException(file);
    return ObjectUtils.openObject(blobId, reader);
  }

  @Nonnull
  public static InputStream openFile(@Nonnull String file, @Nonnull AnyObjectId commit, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return openFile(file, commit, reader);
    }
  }

  @Nonnull
  public static InputStream openFile(@Nonnull String file, @Nonnull String commit, @Nonnull Repository repo) throws IOException {
    return openFile(file, repo.resolve(commit), repo);
  }

  @Nullable
  public static byte[] readFile(@Nonnull String file, @Nonnull AnyObjectId commit, @Nonnull ObjectReader reader) throws IOException {
    AnyObjectId blobId = ObjectUtils.findObject(file, commit, reader);
    if(blobId == null)
      return null;
    return ObjectUtils.readObject(blobId, reader);
  }

  @Nullable
  public static byte[] readFile(@Nonnull String file, @Nonnull AnyObjectId commit, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return readFile(file, commit, reader);
    }
  }

  @Nullable
  public static byte[] readFile(@Nonnull String file, @Nonnull String commit, @Nonnull Repository repo) throws IOException {
    return readFile(file, repo.resolve(commit), repo);
  }

}
