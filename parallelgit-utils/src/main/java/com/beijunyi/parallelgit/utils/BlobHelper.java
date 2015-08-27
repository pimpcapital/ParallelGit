package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.*;

public final class BlobHelper {

  /**
   * Calculates the blob id of the given byte array.
   *
   * @param blob a byte array
   * @return a blob id
   */
  @Nonnull
  public static AnyObjectId getBlobId(@Nonnull byte[] blob) {
    return new ObjectInserter.Formatter().idFor(Constants.OBJ_BLOB, blob);
  }

  /**
   * Calculates the blob id of the given string.
   *
   * This method invokes {@link Constants#encode(String)}, which uses utf-8 to convert the given string into a byte
   * array. The result byte array is processed by {@link #getBlobId(byte[])}.
   *
   * @param content a string
   * @return a blob id
   */
  @Nonnull
  public static AnyObjectId getBlobId(@Nonnull String content) {
    return getBlobId(Constants.encode(content));
  }

  /**
   * Finds the blob id of the specified file in the given commit.
   *
   * @param reader an object reader
   * @param commitId a commit id
   * @param path a file path
   * @return a blob id
   */
  @Nullable
  public static AnyObjectId findBlobId(@Nonnull ObjectReader reader, @Nonnull AnyObjectId commitId, @Nonnull String path) throws IOException {
    return TreeWalkHelper.getObject(reader, path, RevTreeHelper.getRootTree(reader, commitId));
  }

  /**
   * Finds the blob id of the specified file in the given commit.
   *
   * This method creates a temporary {@link ObjectReader} and then invokes {@link #findBlobId(ObjectReader, AnyObjectId,
   * String)}. The temporary reader will be released at the end of this method..
   *
   * @param repo a git repository
   * @param path a file path
   * @param commitId a commit id
   * @return a blob id
   */
  @Nullable
  public static AnyObjectId findBlobId(@Nonnull Repository repo, @Nonnull AnyObjectId commitId, @Nonnull String path) throws IOException {
    ObjectReader reader = repo.newObjectReader();
    try {
      return findBlobId(reader, commitId, path);
    } finally {
      reader.release();
    }
  }

  /**
   * Gets the byte array mapped from the blob id.
   *
   * @param reader an object reader
   * @param blobId a blob id
   * @return a byte array
   */
  @Nonnull
  public static byte[] getBytes(@Nonnull ObjectReader reader, @Nonnull AnyObjectId blobId) throws IOException {
    return reader.open(blobId).getBytes();
  }

  /**
   * Gets the byte array mapped from the blob id.
   *
   * This method creates a temporary {@link ObjectReader} and then invokes {@link #getBytes(ObjectReader, AnyObjectId)}.
   * The temporary reader will be released at the end of this method.
   *
   * @param repo a git repository
   * @param blobId a blob id
   * @return a byte array
   */
  @Nonnull
  public static byte[] getBytes(@Nonnull Repository repo, @Nonnull AnyObjectId blobId) throws IOException {
    ObjectReader reader = repo.newObjectReader();
    try {
      return getBytes(reader, blobId);
    } finally {
      reader.release();
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
    ObjectReader reader = repo.newObjectReader();
    try {
      return getBytes(reader, commitId, path);
    } finally {
      reader.release();
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
    ObjectInserter inserter = repo.newObjectInserter();
    try {
      AnyObjectId blobId = inserter.insert(Constants.OBJ_BLOB, blob);
      inserter.flush();
      return blobId;
    } finally {
      inserter.release();
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
