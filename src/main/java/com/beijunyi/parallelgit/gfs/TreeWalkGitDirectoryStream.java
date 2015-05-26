package com.beijunyi.parallelgit.gfs;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.ParallelGitException;
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

  TreeWalkGitDirectoryStream(@Nonnull String pathStr, @Nonnull GitFileStore store, @Nonnull ObjectReader reader, @Nonnull AnyObjectId tree, @Nullable DirectoryStream.Filter<? super Path> filter) throws NotDirectoryException {
    this(pathStr, store, newDirectoryTreeWalk(reader, pathStr, tree), filter);
  }

  @Nonnull
  private static TreeWalk newDirectoryTreeWalk(@Nonnull ObjectReader reader, @Nonnull String pathStr, @Nonnull AnyObjectId tree) throws NotDirectoryException {
    TreeWalk treeWalk;
    if(pathStr.isEmpty())
      treeWalk = TreeWalkHelper.newTreeWalk(reader, tree);
    else {
      treeWalk = TreeWalkHelper.forPath(reader, pathStr, tree);
      if(treeWalk == null || !treeWalk.isSubtree())
        throw new NotDirectoryException(pathStr);
      TreeWalkHelper.enterSubtree(treeWalk);
    }
    return treeWalk;
  }

  @Nonnull
  @Override
  public Iterator<Path> iterator() {
    return new Iterator<Path>() {
      private GitPath next;

      private boolean findNext() {
        while(TreeWalkHelper.next(treeWalk)) {
          GitPath childPath = store.getRoot().resolve(treeWalk.getPathString());
          try {
            if(filter == null || filter.accept(childPath)) {
              next = childPath;
              return true;
            }
          } catch(IOException e) {
            throw new ParallelGitException("Filter could not decide whether to accept path " + childPath, e);
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
