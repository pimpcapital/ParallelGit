package com.beijunyi.parallelgit.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.dircache.*;
import org.eclipse.jgit.lib.*;

public final class DirCacheHelper {

  /**
   * Creates a new in-core {@code DirCache}.
   *
   * This method behaves exactly the same as {@code DirCache.newInCore()}.
   *
   * @return a new in-core dir cache.
   */
  @Nonnull
  public static DirCache newCache() {
    return DirCache.newInCore();
  }

  /**
   * Loads the specified tree into the given {@code DirCache}.
   *
   * @param cache a dir cache
   * @param reader an object reader
   * @param treeId a tree id
   */
  public static void loadTree(@Nonnull DirCache cache, @Nonnull ObjectReader reader, @Nonnull ObjectId treeId) throws IOException {
    addTree(cache, reader, "", treeId);
  }

  /**
   * Loads the root tree of the specified commit into the given {@code DirCache}.
   *
   * @param cache a dir cache
   * @param reader an object reader
   * @param commitId an object id that points to a commit
   */
  public static void loadRevision(@Nonnull DirCache cache, @Nonnull ObjectReader reader, @Nonnull ObjectId commitId) throws IOException {
    loadTree(cache, reader, RevTreeHelper.getRootTree(reader, commitId));
  }

  /**
   * Creates a new {@code DirCache} with content loaded from the given tree.
   *
   * @param reader an object reader
   * @param treeId a tree id
   * @return a new dir cache with content loaded from the given tree
   */
  public static DirCache forTree(@Nonnull ObjectReader reader, @Nonnull ObjectId treeId) throws IOException {
    DirCache cache = newCache();
    loadTree(cache, reader, treeId);
    return cache;
  }

  /**
   * Creates a new {@code DirCache} with content loaded from the given commit's root tree.
   *
   * @param reader an object reader
   * @param commitId an object id that points to a commit
   * @return a new dir cache
   */
  @Nonnull
  public static DirCache forRevision(@Nonnull ObjectReader reader, @Nonnull ObjectId commitId) throws IOException {
    DirCache cache = newCache();
    loadRevision(cache, reader, commitId);
    return cache;
  }

  /**
   * Creates a new {@code DirCache} with content loaded from the given commit's root tree.
   *
   * This method creates a temporary {@code ObjectReader} and then invokes {@link #forRevision(ObjectReader, ObjectId)}.
   * The temporary reader will be released at the end of this method.
   *
   * @param repo a git repository
   * @param commitId an object id that points to a commit
   * @return a new dir cache
   */
  @Nonnull
  public static DirCache forRevision(@Nonnull Repository repo, @Nonnull ObjectId commitId) throws IOException {
    ObjectReader reader = repo.newObjectReader();
    try {
      return forRevision(reader, commitId);
    } finally {
      reader.release();
    }
  }

  /**
   * Creates a new {@code DirCache} with content from the specified commit's root tree.
   *
   * This method finds the {@code ObjectId} of the commit represented by the specified revision string and then invokes
   * {@link #forRevision(Repository, ObjectId)}.
   *
   * @param repo a git repository
   * @param revision a revision string
   * @return a new dir cache
   */
  @Nonnull
  public static DirCache forRevision(@Nonnull Repository repo, @Nonnull String revision) throws IOException {
    ObjectId revisionId = repo.resolve(revision);
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
  public static ObjectId getBlobId(@Nonnull DirCache cache, @Nonnull String path) {
    DirCacheEntry entry = cache.getEntry(path);
    if(entry == null)
      return null;
    return entry.getObjectId();
  }

  /**
   * Adds the specified tree into the given {@code DirCacheBuilder}.
   *
   * @param builder a dir cache builder
   * @param reader an object reader
   * @param path a directory path
   * @param treeId an object id that points to a tree
   */
  public static void addTree(@Nonnull DirCacheBuilder builder, @Nonnull ObjectReader reader, @Nonnull String path, @Nonnull ObjectId treeId) throws IOException {
    builder.addTree(path.getBytes(), DirCacheEntry.STAGE_0, reader, treeId);
  }

  /**
   * Adds the specified tree into the given {@code DirCache}.
   *
   * This method creates a temporary {@code DirCacheBuilder} and then invokes {@link #addTree(DirCacheBuilder,
   * ObjectReader, String, ObjectId)}. The temporary builder will be finished/flushed at the end of this method..
   *
   * @param cache a dir cache
   * @param reader an object reader
   * @param path a directory path
   * @param treeId an object id that points to a tree
   */
  public static void addTree(@Nonnull DirCache cache, @Nonnull ObjectReader reader, @Nonnull String path, @Nonnull ObjectId treeId) throws IOException {
    DirCacheBuilder builder = keepEverything(cache);
    addTree(builder, reader, path, treeId);
    builder.finish();
  }

  /**
   * Adds a new {@code DirCacheEntry} with the provided blob id into the given {@code DirCacheBuilder} at the specified
   * path.
   *
   * This method always adds the new entry to {@code DirCacheEntry#STAGE_0}.
   *
   * @param builder a dir cache builder
   * @param mode a file mode
   * @param path a file path
   * @param blobId an object id that points to a blob
   */
  public static void addFile(@Nonnull DirCacheBuilder builder, @Nonnull FileMode mode, @Nonnull String path, @Nonnull ObjectId blobId) {
    DirCacheEntry entry = new DirCacheEntry(path, DirCacheEntry.STAGE_0);
    entry.setFileMode(mode);
    entry.setObjectId(blobId);
    builder.add(entry);
  }

  /**
   * Adds a new {@code DirCacheEntry} with the provided blob id into the given {@code DirCache} at the specified path.
   *
   * This method creates a temporary {@code DirCacheBuilder} and then invokes {@link #addFile(DirCacheBuilder, FileMode,
   * String, ObjectId)}. The temporary builder will be finished/flushed at the end of this method..
   *
   * @param cache a dir cache
   * @param mode a file mode
   * @param path a file path
   * @param blobId an object id that points to a blob
   */
  public static void addFile(@Nonnull DirCache cache, @Nonnull FileMode mode, @Nonnull String path, @Nonnull ObjectId blobId) {
    DirCacheBuilder builder = keepEverything(cache);
    addFile(builder, mode, path, blobId);
    builder.finish();
  }

  /**
   * Adds a new {@code DirCacheEntry} with the provided blob id into the given {@code DirCache} at the specified path.
   *
   * This method behaves similarly to {@link #addFile(DirCache, FileMode, String, ObjectId)} except the new entry's file
   * mode is always {@code FileMode.REGULAR_FILE}.
   *
   * @param cache a dir cache
   * @param path a file path
   * @param blobId an object id that points to a blob
   */
  public static void addFile(@Nonnull DirCache cache, @Nonnull String path, @Nonnull ObjectId blobId) {
    addFile(cache, FileMode.REGULAR_FILE, path, blobId);
  }

  /**
   * Adds a {@code DirCacheEditor.DeletePath} edit targeting the specified path to the given {@code DirCacheEditor}.
   *
   * @param editor a dir cache editor
   * @param path a directory path
   */
  public static void deleteFile(@Nonnull DirCacheEditor editor, @Nonnull String path) {
    editor.add(new DirCacheEditor.DeletePath(path));
  }

  /**
   * Deletes the specified file from the given {@code DirCache}.
   *
   * This method creates a temporary {@code DirCacheEditor} and then invokes {@link #deleteFile(DirCacheEditor,
   * String)}. The temporary editor will be finished/flushed at the end of this method..
   *
   * @param cache a dir cache
   * @param path a directory path
   */
  public static void deleteFile(@Nonnull DirCache cache, @Nonnull String path) {
    DirCacheEditor editor = cache.editor();
    deleteFile(editor, path);
    editor.finish();
  }

  /**
   * Adds a {@code DirCacheEditor.DeleteTree} edit targeting the specified path to the given {@code DirCacheEditor}.
   *
   * @param editor a dir cache editor
   * @param path a directory path
   */
  public static void deleteDirectory(@Nonnull DirCacheEditor editor, @Nonnull String path) {
    editor.add(new DirCacheEditor.DeleteTree(path));
  }

  /**
   * Deletes the specified directory from the given {@code DirCache}.
   *
   * This method creates a temporary {@code DirCacheEditor} and then invokes {@link #deleteDirectory(DirCacheEditor,
   * String)}. The temporary editor will be finished/flushed at the end of this method..
   *
   * @param cache a dir cache
   * @param path a directory path
   */
  public static void deleteDirectory(@Nonnull DirCache cache, @Nonnull String path) {
    DirCacheEditor editor = cache.editor();
    deleteDirectory(editor, path);
    editor.finish();
  }

  /**
   * Tests if the specified path exists in the given {@code DirCache}.
   *
   * Note that this method returns {@code false} when the specified path points to a directory.
   *
   * @param cache a dir cache
   * @param path a file path
   * @return {@code true} if the specified path exists in the dir cache
   */
  public static boolean fileExists(@Nonnull DirCache cache, @Nonnull String path) {
    return cache.findEntry(path) >= 0;
  }

  /**
   * Tests if the specified path points to non-trivial(non-empty) directory.
   *
   * {@code DirCache} does not allow empty directory. When all the files in a directory are deleted, this directory
   * becomes trivial as all entries whose paths start from this directory are removed. On the other hand, there is
   * little point to create an empty parent directory before creating a file inside it. When a file is created, its
   * parent directories are automatically created.
   *
   * @param cache a dir cache
   * @param path a file path
   * @return {@code true} if the specified path points to a non-empty directory
   */
  public static boolean isNonTrivialDirectory(@Nonnull DirCache cache, @Nonnull String path) {
    if(path.length() == 0) // if it is root
      return true;

    if(!path.endsWith("/"))
      path += "/";

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

}
