package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.io.BlobSnapshot;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;

public final class GitFileUtils {

  public static boolean exists(@Nonnull String file, @Nonnull AnyObjectId commit, @Nonnull ObjectReader reader) throws IOException {
    return TreeUtils.exists(file, getRootTree(commit, reader), reader);
  }

  public static boolean exists(@Nonnull String file, @Nonnull AnyObjectId commit, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return exists(file, commit, reader);
    }
  }

  public static boolean exists(@Nonnull String file, @Nonnull String commit, @Nonnull Repository repo) throws IOException {
    return exists(file, repo.resolve(commit), repo);
  }

  public static boolean isDirectory(@Nonnull String file, @Nonnull AnyObjectId commit, @Nonnull ObjectReader reader) throws IOException {
    return TreeUtils.isDirectory(file, getRootTree(commit, reader), reader);
  }

  public static boolean isDirectory(@Nonnull String file, @Nonnull AnyObjectId commit, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return isDirectory(file, commit, reader);
    }
  }

  public static boolean isDirectory(@Nonnull String file, @Nonnull String commit, @Nonnull Repository repo) throws IOException {
    return isDirectory(file, repo.resolve(commit), repo);
  }

  public static boolean isFile(@Nonnull String file, @Nonnull AnyObjectId commit, @Nonnull ObjectReader reader) throws IOException {
    return TreeUtils.isRegularOrExecutableFile(file, getRootTree(commit, reader), reader);
  }

  public static boolean isFile(@Nonnull String file, @Nonnull AnyObjectId commit, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return isFile(file, commit, reader);
    }
  }

  public static boolean isFile(@Nonnull String file, @Nonnull String commit, @Nonnull Repository repo) throws IOException {
    return isFile(file, repo.resolve(commit), repo);
  }

  public static boolean isSymbolicLink(@Nonnull String file, @Nonnull AnyObjectId commit, @Nonnull ObjectReader reader) throws IOException {
    return TreeUtils.isSymbolicLink(file, getRootTree(commit, reader), reader);
  }

  public static boolean isSymbolicLink(@Nonnull String file, @Nonnull AnyObjectId commit, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return isSymbolicLink(file, commit, reader);
    }
  }

  public static boolean isSymbolicLink(@Nonnull String file, @Nonnull String commit, @Nonnull Repository repo) throws IOException {
    return isSymbolicLink(file, repo.resolve(commit), repo);
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
  public static BlobSnapshot readFile(@Nonnull String file, @Nonnull AnyObjectId commit, @Nonnull ObjectReader reader) throws IOException {
    AnyObjectId blobId = ObjectUtils.findObject(file, commit, reader);
    if(blobId == null)
      throw new NoSuchFileException(file);
    return ObjectUtils.readBlob(blobId, reader);
  }

  @Nonnull
  public static BlobSnapshot readFile(@Nonnull String file, @Nonnull AnyObjectId commit, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return readFile(file, commit, reader);
    }
  }

  @Nonnull
  public static BlobSnapshot readFile(@Nonnull String file, @Nonnull String revision, @Nonnull Repository repo) throws IOException {
    return readFile(file, repo.resolve(revision), repo);
  }

  @Nonnull
  private static AnyObjectId getRootTree(@Nonnull AnyObjectId commit, @Nonnull ObjectReader reader) throws IOException {
    return CommitUtils.getCommit(commit, reader).getTree();
  }

}
