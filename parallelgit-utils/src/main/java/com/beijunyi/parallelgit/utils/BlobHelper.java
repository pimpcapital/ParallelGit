package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.*;

public final class BlobHelper {

  @Nonnull
  public static AnyObjectId calculateBlobId(@Nonnull byte[] blob) {
    return new ObjectInserter.Formatter().idFor(Constants.OBJ_BLOB, blob);
  }

  @Nonnull
  public static AnyObjectId calculateBlobId(@Nonnull String content) {
    return calculateBlobId(Constants.encode(content));
  }

  @Nullable
  public static AnyObjectId findBlobId(@Nonnull String path, @Nonnull AnyObjectId commitId, @Nonnull ObjectReader reader) throws IOException {
    return TreeWalkHelper.getObjectId(reader, path, RevTreeHelper.getRootTree(reader, commitId));
  }

  @Nullable
  public static AnyObjectId findBlobId(@Nonnull String path, @Nonnull AnyObjectId commitId, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return findBlobId(path, commitId, reader);
    }
  }

  @Nonnull
  public static byte[] getBlob(@Nonnull AnyObjectId blobId, @Nonnull ObjectReader reader) throws IOException {
    return reader.open(blobId).getBytes();
  }

  @Nonnull
  public static byte[] getBlob(@Nonnull AnyObjectId blobId, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return getBlob(blobId, reader);
    }
  }

  @Nullable
  public static byte[] getFileFromCommit(@Nonnull String path, @Nonnull AnyObjectId commitId, @Nonnull ObjectReader reader) throws IOException {
    AnyObjectId blobId = findBlobId(path, commitId, reader);
    if(blobId == null)
      return null;
    return getBlob(blobId, reader);
  }

  @Nullable
  public static byte[] getFileFromCommit(@Nonnull String path, @Nonnull AnyObjectId commitId, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return getFileFromCommit(path, commitId, reader);
    }
  }

  @Nullable
  public static byte[] getFileFromCommit(@Nonnull String path, @Nonnull String commitId, @Nonnull Repository repo) throws IOException {
    return getFileFromCommit(path, repo.resolve(commitId), repo);
  }

  @Nonnull
  public static AnyObjectId insert(@Nonnull byte[] blob, @Nonnull Repository repo) throws IOException {
    try(ObjectInserter inserter = repo.newObjectInserter()) {
      AnyObjectId blobId = inserter.insert(Constants.OBJ_BLOB, blob);
      inserter.flush();
      return blobId;
    }
  }

  @Nonnull
  public static AnyObjectId insert(@Nonnull String content, @Nonnull Repository repo) throws IOException {
    return insert(Constants.encode(content), repo);
  }

}
