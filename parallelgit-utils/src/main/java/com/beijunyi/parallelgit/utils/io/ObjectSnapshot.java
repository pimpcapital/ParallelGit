package com.beijunyi.parallelgit.utils.io;

import java.io.IOException;
import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectInserter;

public abstract class ObjectSnapshot {

  private AnyObjectId id;

  @Nonnull
  public AnyObjectId getId() {
    if(id == null)
      id = computeId();
    return id;
  }

  @Nonnull
  public AnyObjectId insert(@Nonnull ObjectInserter inserter) throws IOException {
    id = persist(inserter);
    return id;
  }

  @Nonnull
  protected abstract AnyObjectId persist(@Nonnull ObjectInserter inserter) throws IOException;

  @Nonnull
  protected abstract AnyObjectId computeId();

}
