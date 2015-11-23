package com.beijunyi.parallelgit.filesystem;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.ClosedFileSystemException;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.ObjectUtils;
import com.beijunyi.parallelgit.utils.io.BlobSnapshot;
import com.beijunyi.parallelgit.utils.io.GitFileEntry;
import com.beijunyi.parallelgit.utils.io.TreeSnapshot;
import org.eclipse.jgit.lib.*;

public class GfsDataService implements Closeable {

  private final Repository repo;
  private final ObjectReader reader;
  private final ObjectInserter inserter;

  private volatile boolean closed = false;

  GfsDataService(@Nonnull final Repository repo) {
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
  public AnyObjectId saveBlob(@Nonnull byte[] bytes) throws IOException {
    checkClosed();
    synchronized(inserter) {
      return inserter.insert(Constants.OBJ_BLOB, bytes);
    }
  }

  @Nonnull
  public AnyObjectId saveTree(@Nonnull TreeSnapshot tree) throws IOException {
    checkClosed();
    TreeFormatter formatter = new TreeFormatter();
    for(Map.Entry<String, GitFileEntry> child : new TreeMap<>(tree.getChildren()).entrySet()) {
      String name = child.getKey();
      GitFileEntry entry = child.getValue();
      formatter.append(name, entry.getMode(), entry.getId());
    }
    synchronized(inserter) {
      return inserter.insert(formatter);
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

  private void checkClosed() {
    if(closed)
      throw new ClosedFileSystemException();
  }


}
