package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.ParallelGitException;
import org.eclipse.jgit.lib.*;

public final class BlobHelper {

  /**
   * Calculates the blob id of the given byte array.
   *
   * @param blob a byte array
   * @return a blob id
   */
  @Nonnull
  public static ObjectId calculateBlobId(@Nonnull byte[] blob) {
    return new ObjectInserter.Formatter().idFor(Constants.OBJ_BLOB, blob);
  }

  /**
   * Calculates the blob id of the given string.
   *
   * This method invokes {@link Constants#encode(String)}, which uses utf-8 to convert the given string into a byte
   * array. The result byte array is processed by {@link #calculateBlobId(byte[])}.
   *
   * @param content a string
   * @return a blob id
   */
  @Nonnull
  public static ObjectId calculateBlobId(@Nonnull String content) {
    return calculateBlobId(Constants.encode(content));
  }

  /**
   * Finds the blob id of the specified file in the given commit.
   *
   * @param reader an object reader
   * @param path a file path
   * @param commitId a commit id
   * @return a blob id
   */
  @Nullable
  public static ObjectId findBlobId(@Nonnull ObjectReader reader, @Nonnull String path, @Nonnull ObjectId commitId) {
    return TreeWalkHelper.getObjectId(reader, path, RevTreeHelper.getTree(reader, commitId));
  }

  /**
   * Finds the blob id of the specified file in the given commit.
   *
   * This method creates a temporary {@link ObjectReader} and then invokes {@link #findBlobId(ObjectReader, String,
   * ObjectId)}. The temporary reader will be released at the end of this method..
   *
   * @param repo a git repository
   * @param path a file path
   * @param commitId a commit id
   * @return a blob id
   */
  @Nullable
  public static ObjectId findBlobId(@Nonnull Repository repo, @Nonnull String path, @Nonnull ObjectId commitId) {
    ObjectReader reader = repo.newObjectReader();
    try {
      return findBlobId(reader, path, commitId);
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
  public static byte[] getBytes(@Nonnull ObjectReader reader, @Nonnull ObjectId blobId) {
    try {
      return reader.open(blobId).getBytes();
    } catch(IOException e) {
      throw new ParallelGitException("Could not get data mapped from " + blobId, e);
    }
  }

  /**
   * Gets the byte array mapped from the blob id.
   *
   * This method creates a temporary {@link ObjectReader} and then invokes {@link #getBytes(ObjectReader, ObjectId)}.
   * The temporary reader will be released at the end of this method.
   *
   * @param repo a git repository
   * @param blobId a blob id
   * @return a byte array
   */
  @Nonnull
  public static byte[] getBytes(@Nonnull Repository repo, @Nonnull ObjectId blobId) {
    ObjectReader reader = repo.newObjectReader();
    try {
      return getBytes(reader, blobId);
    } finally {
      reader.release();
    }
  }

  /**
   * Inserts the given byte array into the repository via the provided {@link ObjectInserter}
   *
   * This method behaves similarly to {@link ObjectInserter#insert(int, byte[])} except the object type is always {@link
   * Constants#OBJ_BLOB}. To be exception friendly, this method does not throw any checked exception. In the case that
   * an {@link IOException} does occur, the source exception can be retrieved from {@link
   * ParallelGitException#getCause()}.
   *
   * @param inserter an object inserter
   * @param blob a byte array
   * @return a blob id which maps to the inserted blob object
   */
  @Nonnull
  public static ObjectId insert(@Nonnull ObjectInserter inserter, @Nonnull byte[] blob) {
    try {
      return inserter.insert(Constants.OBJ_BLOB, blob);
    } catch(IOException e) {
      throw new ParallelGitException("Could not insert blob", e);
    }
  }

  /**
   * Inserts the given byte array into the provided repository.
   *
   * This method creates a temporary {@link ObjectInserter} and then invokes {@link #insert(ObjectInserter, byte[])}.
   * The temporary inserter will be flushed and released at the end of this method..
   *
   * @param repo a git repository
   * @param blob a byte array
   * @return a blob id which maps to the inserted blob object
   */
  @Nonnull
  public static ObjectId insert(@Nonnull Repository repo, @Nonnull byte[] blob) {
    ObjectInserter inserter = repo.newObjectInserter();
    ObjectId blobId = insert(inserter, blob);
    RepositoryHelper.flush(inserter);
    inserter.release();
    return blobId;
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
  public static ObjectId insert(@Nonnull Repository repo, @Nonnull String content) {
    return insert(repo, Constants.encode(content));
  }

}
