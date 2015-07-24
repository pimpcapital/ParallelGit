package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
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

  public GitDirectoryStream(@Nonnull Iterator<Node> nodeIterator, @Nullable Filter<? super Path> filter, @Nonnull DirectoryNode parent) {
    this.nodeIterator = nodeIterator;
    this.filter = filter;
    this.parent = parent;
  }

  @Nonnull
  @Override
  public Iterator<Path> iterator() {
    return new Iterator<Path>() {

      private Node next;
      private Node current;

      private boolean findNext() {
        while(nodeIterator.hasNext()) {
          Node node = nodeIterator.next();
          GitPath path = node.getPath();
          try {
            if(filter == null || filter.accept(path)) {
              next = node;
              return true;
            }
          } catch(IOException e) {
            throw new RuntimeException(e);
          }
        }
        return false;
      }

      @Override
      public boolean hasNext() {
        return next != null || findNext();
      }

      @Nonnull
      @Override
      public Path next() {
        if(next != null || hasNext()) {
          GitPath path = next.getPath();
          current = next;
          next = null;
          return path;
        }
        throw new NoSuchElementException();
      }

      @Override
      public void remove() {
        if(current == null)
          throw new IllegalStateException();
        try {
          current.delete();
        } catch(IOException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  @Override
  public void close() throws IOException {
    parent.removeStream(this);
  }

}
