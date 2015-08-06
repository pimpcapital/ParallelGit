package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.file.ClosedDirectoryStreamException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitPath;

public class GitDirectoryStream implements DirectoryStream<Path> {

  private final GitPath parent;
  private final Collection<String> children;
  private final Filter<? super Path> filter;
  private volatile boolean closed = false;

  public GitDirectoryStream(@Nonnull GitPath parent, @Nonnull Collection<String> children, @Nullable Filter<? super Path> filter) {
    this.parent = parent;
    this.children = children;
    this.filter = filter;
  }

  private void checkNotClosed() throws ClosedDirectoryStreamException {
    if(closed)
      throw new ClosedDirectoryStreamException();
  }

  @Nonnull
  @Override
  public Iterator<Path> iterator() {
    final Iterator<String> childrenIt = new TreeSet<>(children).iterator();
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

}
