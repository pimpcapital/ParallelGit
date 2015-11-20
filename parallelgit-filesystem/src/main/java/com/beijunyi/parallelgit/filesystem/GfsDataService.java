package com.beijunyi.parallelgit.filesystem;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.ClosedFileSystemException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.io.Node;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

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
  public byte[] readBlob(@Nonnull AnyObjectId id) throws IOException {
    checkClosed();
    synchronized(reader) {
      return reader.open(id).getBytes();
    }
  }

  public long getBlobSize(@Nonnull AnyObjectId id) throws IOException {
    checkClosed();
    synchronized(reader) {
      return reader.getObjectSize(id, Constants.OBJ_BLOB);
    }
  }

  @Nonnull
  public Map<String, Node> readTree(@Nonnull AnyObjectId id) throws IOException {
    checkClosed();
    synchronized(reader) {
      Map<String, Node> children = new HashMap<>();
      CanonicalTreeParser treeParser = new CanonicalTreeParser();
      treeParser.reset(reader, id);
      while(!treeParser.eof()) {
        Node child = Node.forObject(treeParser.getEntryObjectId(), treeParser.getEntryFileMode(), this);
        child.takeSnapshot();
        children.put(treeParser.getEntryPathString(), child);
        treeParser.next();
      }
      return children;
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
  public AnyObjectId saveTree(@Nonnull TreeFormatter tf) throws IOException {
    checkClosed();
    synchronized(inserter) {
      return inserter.insert(tf);
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
