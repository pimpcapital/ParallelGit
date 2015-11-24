package com.beijunyi.parallelgit.utils.io;

import java.io.IOException;
import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.*;

public class BlobSnapshot implements ObjectSnapshot {

  private byte[] bytes;

  private BlobSnapshot(@Nonnull byte[] bytes) {
    this.bytes = bytes;
  }

  @Nonnull
  public byte[] getBytes() {
    return bytes;
  }

  @Nonnull
  @Override
  public AnyObjectId getId() {
    return new ObjectInserter.Formatter().idFor(Constants.OBJ_BLOB, bytes);
  }

  @Nonnull
  @Override
  public AnyObjectId save(@Nonnull ObjectInserter inserter) throws IOException {
    return inserter.insert(Constants.OBJ_BLOB, bytes);
  }

  @Nonnull
  public static BlobSnapshot load(@Nonnull AnyObjectId id, @Nonnull ObjectReader reader) throws IOException {
    return new BlobSnapshot(reader.open(id).getBytes());
  }

  @Nonnull
  public static BlobSnapshot capture(@Nonnull byte[] bytes) {
    return new BlobSnapshot(bytes);
  }

}
