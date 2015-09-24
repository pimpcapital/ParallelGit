package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.*;

public final class ObjectUtils {


  @Nonnull
  public static AnyObjectId calculateBlobId(@Nonnull byte[] data) {
    return new ObjectInserter.Formatter().idFor(Constants.OBJ_BLOB, data);
  }

  @Nonnull
  public static AnyObjectId calculateBlobId(@Nonnull String data) {
    return calculateBlobId(Constants.encode(data));
  }

  @Nonnull
  public static AnyObjectId insertBlob(@Nonnull byte[] data, @Nonnull Repository repo) throws IOException {
    try(ObjectInserter inserter = repo.newObjectInserter()) {
      AnyObjectId blobId = inserter.insert(Constants.OBJ_BLOB, data);
      inserter.flush();
      return blobId;
    }
  }

  @Nonnull
  public static AnyObjectId insertBlob(@Nonnull String data, @Nonnull Repository repo) throws IOException {
    return insertBlob(Constants.encode(data), repo);
  }

  @Nullable
  public static AnyObjectId findObject(@Nonnull String file, @Nonnull AnyObjectId commit, @Nonnull ObjectReader reader) throws IOException {
    return TreeUtils.getObjectId(reader, file, RevTreeUtils.getRootTree(reader, commit));
  }

  @Nullable
  public static AnyObjectId findObject(@Nonnull String file, @Nonnull AnyObjectId commit, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return findObject(file, commit, reader);
    }
  }

  @Nonnull
  public static InputStream openObject(@Nonnull AnyObjectId id, @Nonnull ObjectReader reader) throws IOException {
    return reader.open(id).openStream();
  }

  @Nonnull
  public static InputStream openObject(@Nonnull AnyObjectId id, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return openObject(id, reader);
    }
  }

  @Nonnull
  public static byte[] readObject(@Nonnull AnyObjectId id, @Nonnull ObjectReader reader) throws IOException {
    return reader.open(id).getBytes();
  }

  @Nonnull
  public static byte[] readObject(@Nonnull AnyObjectId id, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return readObject(id, reader);
    }
  }



}
