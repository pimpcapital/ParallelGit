package com.beijunyi.parallelgit.utils.io;

import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.annotation.Nonnull;

import org.eclipse.jgit.dircache.DirCacheEntry;

public class CacheIterator implements Iterator<CacheNode> {

  private final DirCacheEntry[] entries;
  private final int directParentLength;

  private CacheIterator(DirCacheEntry[] entries, int directParentLength) {
    this.entries = entries;
    this.directParentLength = directParentLength;
  }

  public CacheIterator(DirCacheEntry[] entries, String directory) {
    this(entries, directory.length() + 2); // ("/" + path + "/").length()
  }

  public CacheIterator(DirCacheEntry[] entries) {
    this(entries, -1);
  }

  private int index = 0;
  private CacheNode prev;
  private CacheNode next;

  public boolean findNext() {
    while(index < entries.length) {
      DirCacheEntry entry = entries[index++];
      String nextPath = "/" + entry.getPathString();
      if(directParentLength == -1)
        next = CacheNode.file(nextPath, entry);
      else {
        if(prev != null && prev.hasChild(nextPath))
          continue;
        int end = nextPath.indexOf('/', directParentLength);
        next = end != -1 ? CacheNode.directory(nextPath.substring(0, end)) : CacheNode.file(nextPath, entry);
      }
      return true;
    }
    return false;
  }

  @Override
  public boolean hasNext() {
    return next != null || findNext();
  }

  @Nonnull
  @Override
  public CacheNode next() {
    if(next != null || findNext()) {
      prev = next;
      next = null;
      return prev;
    }
    throw new NoSuchElementException();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

}
