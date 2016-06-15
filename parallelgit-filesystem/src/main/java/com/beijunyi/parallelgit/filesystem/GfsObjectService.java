package com.beijunyi.parallelgit.filesystem;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.ClosedFileSystemException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.BlobUtils;
import com.beijunyi.parallelgit.utils.io.*;
import org.eclipse.jgit.lib.*;

import static org.eclipse.jgit.lib.Constants.*;

public class GfsObjectService implements Closeable {

  private final Repository repo;
  private final ObjectReader reader;
  private final ObjectInserter inserter;

  private volatile boolean closed = false;

  GfsObjectService(final Repository repo) {
    this.repo = repo;
    this.reader = repo.newObjectReader();
    this.inserter = repo.newObjectInserter();
  }

  @Nonnull
  public Repository getRepository() {
    return repo;
  }

  @Nonnull
  public ObjectLoader open(AnyObjectId objectId) throws IOException {
    checkClosed();
    synchronized(reader) {
      return reader.open(objectId);
    }
  }

  public boolean hasObject(AnyObjectId objectId) throws IOException {
    checkClosed();
    synchronized(reader) {
      return reader.has(objectId);
    }
  }

  @Nonnull
  public <S extends ObjectSnapshot> S read(ObjectId id, Class<S> type) throws IOException {
    if(BlobSnapshot.class.isAssignableFrom(type))
      return type.cast(readBlob(id));
    if(TreeSnapshot.class.isAssignableFrom(type))
      return type.cast(readTree(id));
    throw new UnsupportedOperationException(type.getName());
  }

  @Nonnull
  public BlobSnapshot readBlob(ObjectId id) throws IOException {
    checkClosed();
    synchronized(reader) {
      return BlobUtils.readBlob(id, reader);
    }
  }

  public long getBlobSize(ObjectId id) throws IOException {
    checkClosed();
    synchronized(reader) {
      return BlobUtils.getBlobSize(id, reader);
    }
  }

  @Nonnull
  public TreeSnapshot readTree(ObjectId id) throws IOException {
    checkClosed();
    synchronized(reader) {
      return TreeSnapshot.load(id, reader);
    }
  }

  @Nonnull
  public ObjectId write(ObjectSnapshot snapshot) throws IOException {
    return snapshot.save(inserter);
  }

  public void pullObject(ObjectId id, boolean flush, GfsObjectService sourceObjService) throws IOException {
    if(!hasObject(id)) {
      ObjectLoader loader = sourceObjService.open(id);
      switch(loader.getType()) {
        case OBJ_TREE:
          pullTree(id, sourceObjService);
          break;
        case OBJ_BLOB:
          pullBlob(id, sourceObjService);
          break;
        default:
          throw new UnsupportedOperationException(id.toString());
      }
      if(flush) flush();
    }
  }

  public void pullObject(ObjectId id, GfsObjectService sourceObjService) throws IOException {
    pullObject(id, true, sourceObjService);
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

  private void pullTree(ObjectId id, GfsObjectService sourceObjService) throws IOException {
    TreeSnapshot tree = sourceObjService.readTree(id);
    for(GitFileEntry entry : tree.getData().values())
      pullObject(entry.getId(), false, sourceObjService);
    write(tree);
  }

  private void pullBlob(ObjectId id, GfsObjectService sourceObjService) throws IOException {
    write(sourceObjService.readBlob(id));
  }

  private void checkClosed() {
    if(closed) throw new ClosedFileSystemException();
  }

}
