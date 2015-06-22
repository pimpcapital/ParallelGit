package com.beijunyi.parallelgit.filesystems;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.TreeWalkHelper;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.treewalk.TreeWalk;

public class TreeWalkGitDirectoryStream extends GitDirectoryStream {

  private final TreeWalk treeWalk;
  private final DirectoryStream.Filter<? super Path> filter;

  private TreeWalkGitDirectoryStream(@Nonnull String pathStr, @Nonnull GitFileStore store, @Nonnull TreeWalk treeWalk, @Nullable DirectoryStream.Filter<? super Path> filter) {
    super(pathStr, store);
    this.treeWalk = treeWalk;
    this.filter = filter;
  }

  TreeWalkGitDirectoryStream(@Nonnull String pathStr, @Nonnull GitFileStore store, @Nonnull ObjectReader reader, @Nonnull AnyObjectId tree, @Nullable DirectoryStream.Filter<? super Path> filter) throws IOException {
    this(pathStr, store, newDirectoryTreeWalk(reader, pathStr, tree), filter);
  }

  /**
   *
   * @param reader
   * @param pathStr
   * @param tree
   * @return
   * @throws IOException
   * @throws NotDirectoryException
   */
  @Nonnull
  private static TreeWalk newDirectoryTreeWalk(@Nonnull ObjectReader reader, @Nonnull String pathStr, @Nonnull AnyObjectId tree) throws IOException {
    TreeWalk treeWalk;
    if(pathStr.isEmpty())
      treeWalk = TreeWalkHelper.newTreeWalk(reader, tree);
    else {
      treeWalk = TreeWalk.forPath(reader, pathStr, tree);
      if(treeWalk == null || !treeWalk.isSubtree())
        throw new NotDirectoryException(pathStr);
      treeWalk.enterSubtree();
    }
    return treeWalk;
  }

  @Nonnull
  @Override
  public Iterator<Path> iterator() {
    return new Iterator<Path>() {
      private GitPath next;

      private boolean findNext() throws IOException {
        while(treeWalk.next()) {
          GitPath childPath = store.getRoot().resolve(treeWalk.getPathString());
          if(filter == null || filter.accept(childPath)) {
            next = childPath;
            return true;
          }
        }
        return false;
      }

      @Override
      public boolean hasNext() {
        try {
          return next != null || findNext();
        } catch(IOException e) {
          return false;
        }
      }

      @Nonnull
      @Override
      public Path next() {
        if(next != null || hasNext()) {
          GitPath ret = next;
          next = null;
          return ret;
        }
        throw new NoSuchElementException();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public void close() {
    treeWalk.release();
    super.close();
  }

}
