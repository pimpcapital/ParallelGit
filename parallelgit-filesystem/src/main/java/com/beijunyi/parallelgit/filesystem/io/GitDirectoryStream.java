package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.file.ClosedDirectoryStreamException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitPath;
import com.beijunyi.parallelgit.filesystem.hierarchy.DirectoryNode;
import com.beijunyi.parallelgit.filesystem.hierarchy.Node;

public class GitDirectoryStream implements DirectoryStream<Path> {

  private final Iterator<Node> nodeIterator;
  private final Filter<? super Path> filter;
  private final DirectoryNode parent;
  private volatile boolean closed = false;

  public GitDirectoryStream(@Nonnull Iterator<Node> nodeIterator, @Nullable Filter<? super Path> filter, @Nonnull DirectoryNode parent) {
    this.nodeIterator = nodeIterator;
    this.filter = filter;
    this.parent = parent;
  }

  private void checkNotClosed() throws ClosedDirectoryStreamException {
    if(closed)
      throw new ClosedDirectoryStreamException();
  }

  @Nonnull
  @Override
  public Iterator<Path> iterator() {
    return new Iterator<Path>() {

      private Node next;

      private boolean findNext() {
        while(nodeIterator.hasNext()) {
          Node node = nodeIterator.next();
          GitPath path = node.path();
          try {
            if(filter == null || filter.accept(path)) {
              next = node;
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
          GitPath path = next.path();
          next = null;
          return path;
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
  public synchronized void close() throws IOException {
    if(!closed) {
      closed = true;
      parent.removeStream(this);
    }
  }

}
