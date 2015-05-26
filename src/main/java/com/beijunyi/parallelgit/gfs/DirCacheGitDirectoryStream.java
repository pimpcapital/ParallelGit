package com.beijunyi.parallelgit.gfs;

import java.nio.file.DirectoryStream;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.DirCacheHelper;
import com.beijunyi.parallelgit.utils.VirtualDirCacheEntry;
import org.eclipse.jgit.dircache.DirCache;

public class DirCacheGitDirectoryStream extends GitDirectoryStream {

  private final Iterator<VirtualDirCacheEntry> entryIterator;
  private final DirectoryStream.Filter<? super Path> filter;

  private DirCacheGitDirectoryStream(@Nonnull String pathStr, @Nonnull GitFileStore store, @Nonnull Iterator<VirtualDirCacheEntry> entryIterator, @Nullable DirectoryStream.Filter<? super Path> filter) {
    super(pathStr, store);
    this.filter = filter;
    this.entryIterator = entryIterator;
  }

  DirCacheGitDirectoryStream(@Nonnull String pathStr, @Nonnull GitFileStore store, @Nonnull DirCache cache, @Nullable DirectoryStream.Filter<? super Path> filter) throws NotDirectoryException {
    this(pathStr, store, newVirtualDirEntryIterator(cache, pathStr), filter);
  }

  @Nonnull
  private static Iterator<VirtualDirCacheEntry> newVirtualDirEntryIterator(@Nonnull DirCache cache, @Nonnull String pathStr) throws NotDirectoryException {
    Iterator<VirtualDirCacheEntry> it = DirCacheHelper.iterateDirectory(cache, pathStr);
    if(it == null)
      throw new NotDirectoryException(pathStr);
    return it;
  }

  @Nonnull
  @Override
  public Iterator<Path> iterator() {
    return new Iterator<Path>() {
      private GitPath next;
      private boolean findNext() {
        while(entryIterator.hasNext()) {
          GitPath childPath = store.getRoot().resolve(entryIterator.next().getPath());
          try {
            if(filter == null || filter.accept(childPath)) {
              next = childPath;
              return true;
            }
          } catch(Exception e) {
            throw new GitFileSystemException("Could not test " + childPath, e);
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
    super.close();
  }
}
