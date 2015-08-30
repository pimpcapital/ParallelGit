package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.*;

public final class BlobHelper {

  @Nonnull
  public static AnyObjectId getBlobId(@Nonnull byte[] blob) {
    return new ObjectInserter.Formatter().idFor(Constants.OBJ_BLOB, blob);
  }

  @Nonnull
  public static AnyObjectId getBlobId(@Nonnull String content) {
    return getBlobId(Constants.encode(content));
  }

  @Nullable
  public static AnyObjectId findBlobId(@Nonnull ObjectReader reader, @Nonnull AnyObjectId commitId, @Nonnull String path) throws IOException {
    return TreeWalkHelper.getObject(reader, path, RevTreeHelper.getRootTree(reader, commitId));
  }

  @Nullable
  public static AnyObjectId findBlobId(@Nonnull Repository repo, @Nonnull AnyObjectId commitId, @Nonnull String path) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return findBlobId(reader, commitId, path);
    }
  }

  @Nonnull
  public static byte[] getBytes(@Nonnull ObjectReader reader, @Nonnull AnyObjectId blobId) throws IOException {
    return reader.open(blobId).getBytes();
  }

  @Nonnull
  public static byte[] getBytes(@Nonnull Repository repo, @Nonnull AnyObjectId blobId) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return getBytes(reader, blobId);
    }
  }

  @Nullable
  public static byte[] getBytes(@Nonnull ObjectReader reader, @Nonnull AnyObjectId commitId, @Nonnull String path) throws IOException {
    AnyObjectId blobId = findBlobId(reader, commitId, path);
    if(blobId == null)
      return null;
    return getBytes(reader, blobId);
  }

  @Nullable
  public static byte[] getBytes(@Nonnull Repository repo, @Nonnull AnyObjectId commitId, @Nonnull String path) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return getBytes(reader, commitId, path);
    }
  }

  @Nullable
  public static byte[] getBytes(@Nonnull Repository repo, @Nonnull String commitId, @Nonnull String path) throws IOException {
    return getBytes(repo, repo.resolve(commitId), path);
  }


  /**
   * Inserts the given byte array into the provided repository.
   *
   * @param repo a git repository
   * @param blob a byte array
   * @return a blob id which maps to the inserted blob object
   */
  @Nonnull
  public static AnyObjectId insert(@Nonnull Repository repo, @Nonnull byte[] blob) throws IOException {
    try(ObjectInserter inserter = repo.newObjectInserter()) {
      AnyObjectId blobId = inserter.insert(Constants.OBJ_BLOB, blob);
      inserter.flush();
      return blobId;
    }
  }

  /**
   * Inserts the given byte array into the provided repository.
   *
   * This method invokes {@link Constants#encode(String)}, which uses utf-8 to convert the given string into a byte
   * array. The result byte array is processed by {@link #insert(Repository, byte[])}.
   *
   * @param repo a git repository
   * @param content a string
   * @return a blob id which maps to the inserted blob object
   */
  @Nonnull
  public static AnyObjectId insert(@Nonnull Repository repo, @Nonnull String content) throws IOException {
    return insert(repo, Constants.encode(content));
  }

}
