package com.beijunyi.parallelgit.utils.io;

import java.io.IOException;
import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectInserter;

public interface ObjectSnapshot {

  @Nonnull
  AnyObjectId getId();

  @Nonnull
  AnyObjectId save(@Nonnull ObjectInserter inserter) throws IOException;

}
