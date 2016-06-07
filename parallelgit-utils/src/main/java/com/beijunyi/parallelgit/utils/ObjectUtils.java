package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.io.BlobSnapshot;
import com.beijunyi.parallelgit.utils.io.TreeSnapshot;
import org.eclipse.jgit.lib.*;

import static org.eclipse.jgit.lib.Constants.OBJ_BLOB;

public final class ObjectUtils {

  public static final ObjectId TRIVIAL_OBJECT = ObjectId.zeroId();

  public static boolean isZeroId(@Nonnull AnyObjectId id) {
    return TRIVIAL_OBJECT.equals(id);
  }

  @Nonnull
  public static ObjectId insertBlob(@Nonnull byte[] data, @Nonnull Repository repo) throws IOException {
    try(ObjectInserter inserter = repo.newObjectInserter()) {
      ObjectId blobId = inserter.insert(OBJ_BLOB, data);
      inserter.flush();
      return blobId;
    }
  }

  @Nonnull
  public static ObjectId insertTree(@Nonnull TreeFormatter tf, @Nonnull Repository repo) throws IOException {
    try(ObjectInserter inserter = repo.newObjectInserter()) {
      ObjectId blobId = inserter.insert(tf);
      inserter.flush();
      return blobId;
    }
  }

  @Nullable
  public static ObjectId findObject(@Nonnull String file, @Nonnull AnyObjectId commit, @Nonnull ObjectReader reader) throws IOException {
    return TreeUtils.getObjectId(file, CommitUtils.getCommit(commit, reader).getTree(), reader);
  }

  @Nullable
  public static ObjectId findObject(@Nonnull String file, @Nonnull AnyObjectId commit, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return findObject(file, commit, reader);
    }
  }

  public static long getBlobSize(@Nonnull AnyObjectId id, @Nonnull ObjectReader reader) throws IOException {
    return reader.getObjectSize(id, OBJ_BLOB);
  }

  public static long getBlobSize(@Nonnull AnyObjectId id, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return getBlobSize(id, reader);
    }
  }

  @Nonnull
  public static InputStream openBlob(@Nonnull AnyObjectId id, @Nonnull ObjectReader reader) throws IOException {
    return reader.open(id).openStream();
  }

  @Nonnull
  public static InputStream openBlob(@Nonnull AnyObjectId id, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return openBlob(id, reader);
    }
  }

  @Nonnull
  public static BlobSnapshot readBlob(@Nonnull ObjectId id, @Nonnull ObjectReader reader) throws IOException {
    return BlobSnapshot.load(id, reader);
  }

  @Nonnull
  public static BlobSnapshot readBlob(@Nonnull ObjectId id, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return readBlob(id, reader);
    }
  }

  @Nonnull
  public static TreeSnapshot readTree(@Nonnull ObjectId id, @Nonnull ObjectReader reader) throws IOException {
    return TreeSnapshot.load(id, reader);
  }

  @Nonnull
  public static TreeSnapshot readTree(@Nonnull ObjectId id, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return readTree(id, reader);
    }
  }

}
