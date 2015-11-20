package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.io.BlobSnapshot;
import com.beijunyi.parallelgit.utils.io.TreeSnapshot;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.treewalk.TreeWalk;

public final class ObjectUtils {


  @Nonnull
  public static AnyObjectId calculateBlobId(@Nonnull byte[] data) {
    return new ObjectInserter.Formatter().idFor(Constants.OBJ_BLOB, data);
  }

  @Nonnull
  public static AnyObjectId insertBlob(@Nonnull byte[] data, @Nonnull Repository repo) throws IOException {
    try(ObjectInserter inserter = repo.newObjectInserter()) {
      AnyObjectId blobId = inserter.insert(Constants.OBJ_BLOB, data);
      inserter.flush();
      return blobId;
    }
  }

  @Nullable
  public static AnyObjectId findObject(@Nonnull String file, @Nonnull AnyObjectId commit, @Nonnull ObjectReader reader) throws IOException {
    return TreeUtils.getObjectId(file, CommitUtils.getCommit(commit, reader).getTree(), reader);
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
  public static BlobSnapshot readBlob(@Nonnull AnyObjectId id, @Nonnull ObjectReader reader) throws IOException {
    return new BlobSnapshot(reader.open(id).getBytes());
  }

  @Nonnull
  public static BlobSnapshot readBlob(@Nonnull AnyObjectId id, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return readBlob(id, reader);
    }
  }

  @Nonnull
  public static TreeSnapshot readTree(@Nonnull AnyObjectId id, @Nonnull ObjectReader reader) throws IOException {
    try(TreeWalk tw = TreeUtils.newTreeWalk(id, reader)) {
      return new TreeSnapshot(tw);
    }
  }

  @Nonnull
  public static TreeSnapshot readTree(@Nonnull AnyObjectId id, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return readTree(id, reader);
    }
  }

}
