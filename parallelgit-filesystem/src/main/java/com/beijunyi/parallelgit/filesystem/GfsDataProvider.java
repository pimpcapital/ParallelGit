package com.beijunyi.parallelgit.filesystem;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.ClosedFileSystemException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.ObjectUtils;
import com.beijunyi.parallelgit.utils.io.*;
import org.eclipse.jgit.lib.*;

import static org.eclipse.jgit.lib.Constants.*;

public class GfsDataProvider implements Closeable {

  private final Repository repo;
  private final ObjectReader reader;
  private final ObjectInserter inserter;

  private volatile boolean closed = false;

  GfsDataProvider(@Nonnull final Repository repo) {
    this.repo = repo;
    this.reader = repo.newObjectReader();
    this.inserter = repo.newObjectInserter();
  }

  @Nonnull
  public Repository getRepository() {
    return repo;
  }

  public boolean hasObject(@Nonnull AnyObjectId objectId) throws IOException {
    checkClosed();
    synchronized(reader) {
      return reader.has(objectId);
    }
  }

  @Nonnull
  public BlobSnapshot readBlob(@Nonnull AnyObjectId id) throws IOException {
    checkClosed();
    synchronized(reader) {
      return ObjectUtils.readBlob(id, reader);
    }
  }

  public long getBlobSize(@Nonnull AnyObjectId id) throws IOException {
    checkClosed();
    synchronized(reader) {
      return ObjectUtils.getBlobSize(id, reader);
    }
  }

  @Nonnull
  public TreeSnapshot readTree(@Nonnull AnyObjectId id) throws IOException {
    checkClosed();
    synchronized(reader) {
      return ObjectUtils.readTree(id, reader);
    }
  }

  @Nonnull
  public AnyObjectId write(@Nonnull ObjectSnapshot snapshot) throws IOException {
    return snapshot.insert(inserter);
  }

  public void pullObject(@Nonnull AnyObjectId id, @Nonnull GfsDataProvider sourceGds) throws IOException {
    if(!hasObject(id)) {
      ObjectLoader loader = sourceGds.reader.open(id);
      switch(loader.getType()) {
        case OBJ_TREE:
          pullTree(id, sourceGds);
          break;
        case OBJ_BLOB:
          pullBlob(id, sourceGds);
          break;
        default:
          throw new UnsupportedOperationException(id.toString());
      }
    }
  }

  public void flush() throws IOException {
    checkClosed();
    synchronized(inserter) {
      inserter.flush();
    }
  }

  @Override
  public synchronized void close() {
    if(!closed) {
      closed = true;
      reader.close();
      inserter.close();
      repo.close();
    }
  }

  private void pullTree(@Nonnull AnyObjectId id, @Nonnull GfsDataProvider sourceGds) throws IOException {
    TreeSnapshot tree = sourceGds.readTree(id);
    for(GitFileEntry entry : tree.getChildren().values())
      pullObject(entry.getId(), sourceGds);
    write(tree);
  }

  private void pullBlob(@Nonnull AnyObjectId id, @Nonnull GfsDataProvider sourceGds) throws IOException {
    write(sourceGds.readBlob(id));
  }

  private void checkClosed() {
    if(closed)
      throw new ClosedFileSystemException();
  }

}
