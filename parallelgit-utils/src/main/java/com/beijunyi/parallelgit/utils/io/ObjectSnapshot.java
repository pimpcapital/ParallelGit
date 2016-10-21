package com.beijunyi.parallelgit.utils.io;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.ObjectInserter.Formatter;
import org.eclipse.jgit.lib.Repository;

public abstract class ObjectSnapshot<Data> {

  protected final ObjectId id;
  protected Data data;

  protected ObjectSnapshot(Data data, @Nullable ObjectId id) {
    this.data = data;
    this.id = id != null ? id : new Formatter().idFor(getType(), toByteArray(data));
  }

  @Nonnull
  public Data getData() throws IOException {
    return data;
  }

  @Nonnull
  public ObjectId getId() {
    return id;
  }

  @Nonnull
  public ObjectId save(Repository repo) throws IOException {
    try(ObjectInserter inserter = repo.newObjectInserter()) {
      ObjectId ret = save(inserter);
      inserter.flush();
      return ret;
    }
  }

  @Nonnull
  public abstract ObjectId save(ObjectInserter inserter) throws IOException;

  protected abstract int getType();

  @Nonnull
  protected abstract byte[] toByteArray(Data data);

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
