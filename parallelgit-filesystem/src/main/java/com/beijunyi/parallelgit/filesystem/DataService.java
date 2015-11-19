package com.beijunyi.parallelgit.filesystem;

import java.io.Closeable;
import java.io.IOException;
import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.*;

public class DataService implements Closeable {

  private final Repository repo;
  private final ThreadLocal<ObjectReader> reader;
  private final ThreadLocal<ObjectInserter> inserter;

  DataService(@Nonnull final Repository repo) {
    this.repo = repo;
    reader = new ThreadLocal<ObjectReader>() {
      @Override
      protected ObjectReader initialValue() {
        return repo.newObjectReader();
      }
    };
    inserter = new ThreadLocal<ObjectInserter>() {
      @Override
      protected ObjectInserter initialValue() {
        return repo.newObjectInserter();
      }
    };
  }

  @Nonnull
  public AnyObjectId saveBlob(@Nonnull byte[] bytes) throws IOException {
    return inserter.get().insert(Constants.OBJ_BLOB, bytes);
  }

  @Nonnull
  public AnyObjectId saveTree(@Nonnull TreeFormatter tf) throws IOException {
    return inserter.get().insert(tf);
  }

  public void flush() throws IOException {
    inserter.get().flush();
  }

  @Override
  public void close() {
    repo.close();
  }


}
