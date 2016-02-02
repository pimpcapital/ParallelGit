package com.beijunyi.parallelgit.utils.io;

import java.io.IOException;
import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.lib.ObjectInserter.Formatter;

import static org.eclipse.jgit.lib.Constants.OBJ_BLOB;

public class BlobSnapshot extends ObjectSnapshot<byte[]> {

  private BlobSnapshot(@Nonnull AnyObjectId id, @Nonnull byte[] bytes) {
    super(id, bytes);
  }

  @Nonnull
  public static BlobSnapshot load(@Nonnull AnyObjectId id, @Nonnull ObjectReader reader) throws IOException {
    return new BlobSnapshot(id, reader.open(id).getBytes());
  }

  @Nonnull
  public static BlobSnapshot capture(@Nonnull byte[] bytes) {
    return new BlobSnapshot(computeBlobId(bytes), bytes);
  }

  @Nonnull
  @Override
  public AnyObjectId persist(@Nonnull ObjectInserter inserter) throws IOException {
    return inserter.insert(OBJ_BLOB, getData());
  }

  @Nonnull
  private static AnyObjectId computeBlobId(@Nonnull byte[] data) {
    return new Formatter().idFor(OBJ_BLOB, data);
  }

}
