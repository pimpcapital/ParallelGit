package com.beijunyi.parallelgit.utils.io;

import java.nio.ByteBuffer;
import javax.annotation.Nonnull;

public class BlobSnapshot {

  private ByteBuffer bytes;

  public BlobSnapshot(@Nonnull byte[] bytes) {
    this(wrap(bytes));
  }

  private BlobSnapshot(@Nonnull ByteBuffer bytes) {
    this.bytes = bytes;
  }

  @Nonnull
  public ByteBuffer getBytes() {
    return bytes;
  }

  @Nonnull
  private static ByteBuffer wrap(@Nonnull byte[] bytes) {
    return ByteBuffer.wrap(bytes).asReadOnlyBuffer();
  }

}
