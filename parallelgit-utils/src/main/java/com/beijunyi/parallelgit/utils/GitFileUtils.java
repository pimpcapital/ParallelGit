package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;

public final class GitFileUtils {

  public static boolean exists(@Nonnull String file, @Nonnull AnyObjectId commit, @Nonnull ObjectReader reader) throws IOException {
    return ObjectUtils.findObject(file, commit, reader) != null;
  }

  public static boolean exists(@Nonnull String file, @Nonnull AnyObjectId commit, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return exists(file, commit, reader);
    }
  }

  public static boolean exists(@Nonnull String file, @Nonnull String commit, @Nonnull Repository repo) throws IOException {
    return exists(file, repo.resolve(commit), repo);
  }

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

  @Nonnull
  public static byte[] readFile(@Nonnull String file, @Nonnull AnyObjectId commit, @Nonnull ObjectReader reader) throws IOException {
    AnyObjectId blobId = ObjectUtils.findObject(file, commit, reader);
    if(blobId == null)
      throw new NoSuchFileException(file);
    return ObjectUtils.readObject(blobId, reader);
  }

  @Nonnull
  public static byte[] readFile(@Nonnull String file, @Nonnull AnyObjectId commit, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return readFile(file, commit, reader);
    }
  }

  @Nonnull
  public static byte[] readFile(@Nonnull String file, @Nonnull String commit, @Nonnull Repository repo) throws IOException {
    return readFile(file, repo.resolve(commit), repo);
  }

}
