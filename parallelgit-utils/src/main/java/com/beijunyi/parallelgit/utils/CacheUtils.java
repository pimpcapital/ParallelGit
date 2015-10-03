package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.dircache.*;
import org.eclipse.jgit.lib.*;

public final class CacheUtils {

  @Nullable
  public static DirCacheEntry getEntry(@Nonnull DirCache cache, @Nonnull String path) {
    return cache.getEntry(TreeUtils.normalizeTreePath(path));
  }

  public static int findEntry(@Nonnull DirCache cache, @Nonnull String path) {
    return cache.findEntry(TreeUtils.normalizeTreePath(path));
  }

  @Nonnull
  public static DirCacheEntry newDirCacheEntry(@Nonnull String path) {
    return new DirCacheEntry(TreeUtils.normalizeTreePath(path));
  }

  @Nonnull
  public static DirCacheEditor.DeletePath deleteEntry(@Nonnull String path) {
    return new DirCacheEditor.DeletePath(TreeUtils.normalizeTreePath(path));
  }

  @Nonnull
  public static DirCacheEditor.DeleteTree deleteChildren(@Nonnull String path) {
    return new DirCacheEditor.DeleteTree(TreeUtils.normalizeTreePath(path));
  }

  public static void loadTree(@Nonnull DirCache cache, @Nonnull ObjectReader reader, @Nonnull AnyObjectId treeId) throws IOException {
    addTree(cache, reader, "", treeId);
  }
  ;
  public static void loadRevision(@Nonnull DirCache cache, @Nonnull ObjectReader reader, @Nonnull AnyObjectId commitId) throws IOException {
    loadTree(cache, reader, CommitUtils.getCommit(commitId, reader).getTree());
  }

  public static DirCache forTree(@Nonnull ObjectReader reader, @Nonnull AnyObjectId treeId) throws IOException {
    DirCache cache = DirCache.newInCore();
    loadTree(cache, reader, treeId);
    return cache;
  }

  @Nonnull
  public static DirCache forRevision(@Nonnull ObjectReader reader, @Nonnull AnyObjectId commitId) throws IOException {
    DirCache cache = DirCache.newInCore();
    loadRevision(cache, reader, commitId);
    return cache;
  }

  @Nonnull
  public static DirCache forRevision(@Nonnull Repository repo, @Nonnull AnyObjectId commitId) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return forRevision(reader, commitId);
    }
  }

  @Nonnull
  public static DirCache forRevision(@Nonnull Repository repo, @Nonnull String revision) throws IOException {
    AnyObjectId revisionId = repo.resolve(revision);
    if(revisionId == null)
      throw new IllegalArgumentException("Could not find matched commit id for " + revision);
    return forRevision(repo, revisionId);
  }

  @Nonnull
  public static DirCacheBuilder keepEverything(@Nonnull DirCache cache) {
    DirCacheBuilder builder = cache.builder();
    int count = cache.getEntryCount();
    if(count > 0)
      builder.keep(0, count);
    return builder;
  }

  @Nullable
  public static AnyObjectId getBlobId(@Nonnull DirCache cache, @Nonnull String path) {
    DirCacheEntry entry = getEntry(cache, path);
    if(entry == null)
      return null;
    return entry.getObjectId();
  }

  @Nullable
  public static FileMode getFileMode(@Nonnull DirCache cache, @Nonnull String path) {
    DirCacheEntry entry = getEntry(cache, path);
    if(entry == null)
      return null;
    return entry.getFileMode();
  }

  public static void addTree(@Nonnull DirCacheBuilder builder, @Nonnull ObjectReader reader, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    builder.addTree(TreeUtils.normalizeTreePath(path).getBytes(), DirCacheEntry.STAGE_0, reader, treeId);
  }

  public static void addTree(@Nonnull DirCache cache, @Nonnull ObjectReader reader, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    DirCacheBuilder builder = keepEverything(cache);
    addTree(builder, reader, path, treeId);
    builder.finish();
  }

  public static void addFile(@Nonnull DirCacheBuilder builder, @Nonnull FileMode mode, @Nonnull String path, @Nonnull AnyObjectId blobId) {
    DirCacheEntry entry = newDirCacheEntry(path);
    entry.setFileMode(mode);
    entry.setObjectId(blobId);
    builder.add(entry);
  }

  public static void addFile(@Nonnull DirCache cache, @Nonnull FileMode mode, @Nonnull String path, @Nonnull AnyObjectId blobId) {
    DirCacheBuilder builder = keepEverything(cache);
    addFile(builder, mode, path, blobId);
    builder.finish();
  }

  public static void addFile(@Nonnull DirCache cache, @Nonnull String path, @Nonnull AnyObjectId blobId) {
    addFile(cache, FileMode.REGULAR_FILE, path, blobId);
  }

  public static void deleteFile(@Nonnull DirCacheEditor editor, @Nonnull String path) {
    editor.add(deleteEntry(path));
  }

  public static void deleteFile(@Nonnull DirCache cache, @Nonnull String path) {
    DirCacheEditor editor = cache.editor();
    deleteFile(editor, path);
    editor.finish();
  }

  public static void deleteDirectory(@Nonnull DirCacheEditor editor, @Nonnull String path) {
    editor.add(deleteChildren(path));
  }

  public static void deleteDirectory(@Nonnull DirCache cache, @Nonnull String path) {
    DirCacheEditor editor = cache.editor();
    deleteDirectory(editor, path);
    editor.finish();
  }

  public static boolean isFile(@Nonnull DirCache cache, @Nonnull String path) {
    return findEntry(cache, path) >= 0;
  }

  public static boolean isSymbolicLink(@Nonnull DirCache cache, @Nonnull String path) {
    DirCacheEntry entry = getEntry(cache, path);
    return entry != null && entry.getFileMode() == FileMode.SYMLINK;
  }

  public static boolean isRegularFile(@Nonnull DirCache cache, @Nonnull String path) {
    DirCacheEntry entry = getEntry(cache, path);
    return entry != null && entry.getFileMode() == FileMode.REGULAR_FILE;
  }

  public static boolean isExecutableFile(@Nonnull DirCache cache, @Nonnull String path) {
    DirCacheEntry entry = getEntry(cache, path);
    return entry != null && entry.getFileMode() == FileMode.EXECUTABLE_FILE;
  }

  public static boolean isRegularOrExecutableFile(@Nonnull DirCache cache, @Nonnull String path) {
    DirCacheEntry entry = getEntry(cache, path);
    return entry != null
             && (entry.getFileMode() == FileMode.REGULAR_FILE || entry.getFileMode() == FileMode.EXECUTABLE_FILE);
  }

  public static boolean isNonTrivialDirectory(@Nonnull DirCache cache, @Nonnull String path) {
    path = TreeUtils.normalizeTreePath(path) + "/";
    if(path.equals("/")) // if it is root
      return true;

    int startIndex = cache.findEntry(path); // find the index of this path
    if(startIndex < 0) // when the path is a directory, its index is negative
      startIndex = -(startIndex + 1); // indices of children paths inside this directory start from -(index + 1)

    if(startIndex >= cache.getEntryCount()) // if beyond the total
      return false;

    String childPath = cache.getEntry(startIndex).getPathString(); // potentially the first child inside this directory
    return childPath.startsWith(path); // if it is indeed a child path, then this directory is non-trivial
  }

  @Nullable
  public static Iterator<VirtualDirCacheEntry> iterateDirectory(@Nonnull final DirCache cache, @Nonnull String path) {
    final DirCacheEntry[] entries = cache.getEntriesWithin(path);
    if(entries.length == 0)
      return null;
    final int childrenMinLength = path.length() + 1;
    return new Iterator<VirtualDirCacheEntry>() {
      private int index = 0;
      private VirtualDirCacheEntry prev;
      private VirtualDirCacheEntry next;

      public boolean findNext() {
        while(index < entries.length) {
          DirCacheEntry entry = entries[index++];
          String path = entry.getPathString();
          if(prev != null && prev.hasChild(path))
            continue;
          int end = path.indexOf('/', childrenMinLength);
          next = end != -1 ? VirtualDirCacheEntry.directory(path.substring(0, end)) : VirtualDirCacheEntry.file(path);
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
      public VirtualDirCacheEntry next() {
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
    };
  }

  @Nonnull
  public static AnyObjectId writeTree(@Nonnull Repository repo, @Nonnull DirCache cache) throws IOException {
    try(ObjectInserter inserter = repo.newObjectInserter()) {
      AnyObjectId tree = cache.writeTree(inserter);
      inserter.flush();
      return tree;
    }
  }

}
