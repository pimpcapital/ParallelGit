package com.beijunyi.parallelgit.utils.io;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectInserter;

public abstract class ObjectSnapshot<Data> {

  protected final AnyObjectId id;
  protected final Data data;

  protected ObjectSnapshot(@Nonnull AnyObjectId id, @Nonnull Data data) {
    this.id = id;
    this.data = data;
  }

  @Nonnull
  public Data getData() {
    return data;
  }

  @Nonnull
  public AnyObjectId getId() {
    return id;
  }

  @Nonnull
  public AnyObjectId insert(@Nonnull ObjectInserter inserter) throws IOException {
    return persist(inserter);
  }

  @Nonnull
  protected abstract AnyObjectId persist(@Nonnull ObjectInserter inserter) throws IOException;

  @Override
  public boolean equals(@Nullable Object that) {
    return this == that ||
             that != null && that instanceof ObjectSnapshot && id.equals(((ObjectSnapshot)that).id);

  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
