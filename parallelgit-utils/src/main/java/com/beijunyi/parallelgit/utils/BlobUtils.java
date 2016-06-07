package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.io.BlobSnapshot;
import org.eclipse.jgit.lib.*;

import static org.eclipse.jgit.lib.Constants.OBJ_BLOB;

public final class BlobUtils {

  @Nonnull
  public static ObjectId insertBlob(byte[] data, Repository repo) throws IOException {
    try(ObjectInserter inserter = repo.newObjectInserter()) {
      ObjectId blobId = inserter.insert(OBJ_BLOB, data);
      inserter.flush();
      return blobId;
    }
  }

  public static long getBlobSize(AnyObjectId id, ObjectReader reader) throws IOException {
    return reader.getObjectSize(id, OBJ_BLOB);
  }

  public static long getBlobSize(AnyObjectId id, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return getBlobSize(id, reader);
    }
  }

  @Nonnull
  public static InputStream openBlob(AnyObjectId id, ObjectReader reader) throws IOException {
    return reader.open(id).openStream();
  }

  @Nonnull
  public static InputStream openBlob(AnyObjectId id, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return openBlob(id, reader);
    }
  }

  @Nonnull
  public static BlobSnapshot readBlob(ObjectId id, ObjectReader reader) throws IOException {
    return BlobSnapshot.load(id, reader);
  }

  @Nonnull
  public static BlobSnapshot readBlob(ObjectId id, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return readBlob(id, reader);
    }
  }

}
