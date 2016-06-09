package com.beijunyi.parallelgit.utils.io;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;

public abstract class ObjectSnapshot<Data> {

  protected final ObjectId id;
  protected final Data data;

  protected ObjectSnapshot(ObjectId id, Data data) {
    this.id = id;
    this.data = data;
  }

  @Nonnull
  public Data getData() {
    return data;
  }

  @Nonnull
  public ObjectId getId() {
    return id;
  }

  @Nonnull
  public ObjectId insert(ObjectInserter inserter) throws IOException {
    return save(inserter);
  }

  @Nonnull
  protected abstract ObjectId save(ObjectInserter inserter) throws IOException;

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
