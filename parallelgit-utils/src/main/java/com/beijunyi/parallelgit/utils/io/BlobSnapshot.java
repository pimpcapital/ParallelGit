package com.beijunyi.parallelgit.utils.io;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.*;

import static org.eclipse.jgit.lib.Constants.OBJ_BLOB;

public class BlobSnapshot extends ObjectSnapshot<byte[]> {

  private BlobSnapshot(byte[] bytes, @Nullable ObjectId id) {
    super(bytes, id);
  }

  private BlobSnapshot(byte[] bytes) {
    this(bytes, null);
  }

  @Nonnull
  public static BlobSnapshot load(ObjectId id, ObjectReader reader) throws IOException {
    return new BlobSnapshot(reader.open(id).getBytes(), id);
  }

  @Nonnull
  public static BlobSnapshot load(ObjectId id, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return load(id, reader);
    }
  }

  @Nonnull
  public static BlobSnapshot capture(byte[] bytes) {
    return new BlobSnapshot(bytes);
  }

  @Nonnull
  @Override
  public ObjectId save(ObjectInserter inserter) throws IOException {
    return inserter.insert(OBJ_BLOB, getData());
  }

  @Override
  protected int getType() {
    return OBJ_BLOB;
  }

  @Nonnull
  @Override
  protected byte[] toByteArray(byte[] bytes) {
    return bytes;
  }

}
