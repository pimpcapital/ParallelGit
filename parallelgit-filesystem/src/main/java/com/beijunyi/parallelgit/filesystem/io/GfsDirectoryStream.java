package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.file.ClosedDirectoryStreamException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitPath;

public class GfsDirectoryStream implements DirectoryStream<Path> {

  private final GitPath parent;
  private final List<String> children;
  private final Filter<? super Path> filter;
  private volatile boolean closed = false;

  public GfsDirectoryStream(DirectoryNode dir, GitPath parent, @Nullable Filter<? super Path> filter) throws IOException {
    this.parent = parent;
    this.filter = filter;
    children = dir.listChildren();
  }

  @Nonnull
  @Override
  public Iterator<Path> iterator() {
    final Iterator<String> childrenIt = children.iterator();
    return new Iterator<Path>() {

      private Path next;

      private boolean findNext() {
        while(childrenIt.hasNext()) {
          String child = childrenIt.next();
          GitPath childPath = parent.resolve(child);
          try {
            if(filter == null || filter.accept(childPath)) {
              next = childPath;
              return true;
            }
          } catch(IOException ignore) {
          }
        }
        return false;
      }

      @Override
      public boolean hasNext() throws ClosedDirectoryStreamException {
        checkNotClosed();
        return next != null || findNext();
      }

      @Nonnull
      @Override
      public Path next() throws ClosedDirectoryStreamException, NoSuchElementException {
        checkNotClosed();
        if(next != null || hasNext()) {
          Path ret = next;
          next = null;
          return ret;
        }
        throw new NoSuchElementException();
      }

      @Override
      public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public void close() throws IOException {
    closed = true;
  }

  private void checkNotClosed() throws ClosedDirectoryStreamException {
    if(closed)
      throw new ClosedDirectoryStreamException();
  }

}
