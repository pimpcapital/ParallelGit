package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.util.Iterator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.exceptions.NoSuchCacheDirectoryException;
import com.beijunyi.parallelgit.utils.exceptions.NoSuchCacheEntryException;
import com.beijunyi.parallelgit.utils.exceptions.NoSuchCommitException;
import com.beijunyi.parallelgit.utils.io.CacheEntryUpdate;
import com.beijunyi.parallelgit.utils.io.CacheIterator;
import com.beijunyi.parallelgit.utils.io.CacheNode;
import org.eclipse.jgit.dircache.*;
import org.eclipse.jgit.lib.*;

public final class CacheUtils {

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

  public static void loadTree(@Nonnull AnyObjectId treeId, @Nonnull DirCache cache, @Nonnull ObjectReader reader) throws IOException {
    addTree("", treeId, cache, reader);
  }

  public static void loadRevision(@Nonnull AnyObjectId revision, @Nonnull DirCache cache, @Nonnull ObjectReader reader) throws IOException {
    loadTree(CommitUtils.getCommit(revision, reader).getTree(), cache, reader);
  }

  @Nonnull
  public static DirCache forTree(@Nonnull AnyObjectId treeId, @Nonnull ObjectReader reader) throws IOException {
    DirCache cache = DirCache.newInCore();
    loadTree(treeId, cache, reader);
    return cache;
  }

  @Nonnull
  public static DirCache forTree(@Nonnull AnyObjectId treeId, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return forTree(treeId, reader);
    }
  }

  @Nonnull
  public static DirCache forRevision(@Nonnull AnyObjectId revision, @Nonnull ObjectReader reader) throws IOException {
    DirCache cache = DirCache.newInCore();
    loadRevision(revision, cache, reader);
    return cache;
  }

  @Nonnull
  public static DirCache forRevision(@Nonnull AnyObjectId revision, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return forRevision(revision, reader);
    }
  }

  @Nonnull
  public static DirCache forRevision(@Nonnull Ref revision, @Nonnull Repository repo) throws IOException {
    return forRevision(revision.getObjectId(), repo);
  }

  @Nonnull
  public static DirCache forRevision(@Nonnull String revision, @Nonnull Repository repo) throws IOException {
    AnyObjectId revisionId = repo.resolve(revision);
    if(revisionId == null)
      throw new NoSuchCommitException(revision);
    return forRevision(revisionId, repo);
  }

  @Nonnull
  public static DirCacheBuilder keepEverything(@Nonnull DirCache cache) {
    DirCacheBuilder builder = cache.builder();
    int count = cache.getEntryCount();
    if(count > 0)
      builder.keep(0, count);
    return builder;
  }

  public static void addTree(@Nonnull String path, @Nonnull AnyObjectId treeId, @Nonnull DirCacheBuilder builder, @Nonnull ObjectReader reader) throws IOException {
    builder.addTree(TreeUtils.normalizeTreePath(path).getBytes(), DirCacheEntry.STAGE_0, reader, treeId);
  }

  public static void addTree(@Nonnull String path, @Nonnull AnyObjectId treeId, @Nonnull DirCache cache, @Nonnull ObjectReader reader) throws IOException {
    DirCacheBuilder builder = keepEverything(cache);
    addTree(path, treeId, builder, reader);
    builder.finish();
  }

  public static void addFile(@Nonnull String path, @Nonnull FileMode mode, @Nonnull AnyObjectId blobId, @Nonnull DirCacheBuilder builder) {
    DirCacheEntry entry = newDirCacheEntry(path);
    entry.setFileMode(mode);
    entry.setObjectId(blobId);
    builder.add(entry);
  }

  public static void addFile(@Nonnull String path, @Nonnull FileMode mode, @Nonnull AnyObjectId blobId, @Nonnull DirCache cache) {
    DirCacheBuilder builder = keepEverything(cache);
    addFile(path, mode, blobId, builder);
    builder.finish();
  }

  public static void addFile(@Nonnull String path, @Nonnull AnyObjectId blobId, @Nonnull DirCache cache) {
    addFile(path, FileMode.REGULAR_FILE, blobId, cache);
  }

  public static void deleteFile(@Nonnull String path, @Nonnull DirCacheEditor editor) {
    editor.add(deleteEntry(path));
  }

  public static void deleteFile(@Nonnull String path, @Nonnull DirCache cache) {
    DirCacheEditor editor = cache.editor();
    deleteFile(path, editor);
    editor.finish();
  }

  public static void deleteDirectory(@Nonnull String path, @Nonnull DirCacheEditor editor) {
    editor.add(deleteChildren(path));
  }

  public static void deleteDirectory(@Nonnull String path, @Nonnull DirCache cache) {
    DirCacheEditor editor = cache.editor();
    deleteDirectory(path, editor);
    editor.finish();
  }

  @Nullable
  public static DirCacheEntry getEntry(@Nonnull String path, @Nonnull DirCache cache) {
    return cache.getEntry(TreeUtils.normalizeTreePath(path));
  }

  public static void updateFile(@Nonnull CacheEntryUpdate update, @Nonnull DirCacheEditor editor) {
    editor.add(update);
  }

  public static void updateFile(@Nonnull CacheEntryUpdate update, @Nonnull DirCache cache) {
    DirCacheEditor editor = cache.editor();
    updateFile(update, editor);
    editor.finish();
  }

  public static void updateFileBlob(@Nonnull String path, @Nonnull ObjectId blob, @Nonnull DirCache cache) {
    updateFile(new CacheEntryUpdate(path).setNewBlob(blob), cache);
  }

  public static void updateFileMode(@Nonnull String path, @Nonnull FileMode mode, @Nonnull DirCache cache) {
    updateFile(new CacheEntryUpdate(path).setNewFileMode(mode), cache);
  }

  public static int findEntry(@Nonnull String path, @Nonnull DirCache cache) {
    return cache.findEntry(TreeUtils.normalizeTreePath(path));
  }

  public static boolean entryExists(@Nonnull String path, @Nonnull DirCache cache) {
    return findEntry(path, cache) >= 0;
  }

  @Nonnull
  public static ObjectId getBlob(@Nonnull String path, @Nonnull DirCache cache) throws NoSuchCacheEntryException {
    return ensureEntry(path, cache).getObjectId();
  }

  @Nonnull
  public static FileMode getFileMode(@Nonnull String path, @Nonnull DirCache cache) throws NoSuchCacheEntryException {
    return ensureEntry(path, cache).getFileMode();
  }


  public static boolean isFile(@Nonnull String path, @Nonnull DirCache cache) {
    return entryExists(path, cache);
  }

  public static boolean isSymbolicLink(@Nonnull String path, @Nonnull DirCache cache) {
    DirCacheEntry entry = getEntry(path, cache);
    return entry != null && entry.getFileMode() == FileMode.SYMLINK;
  }

  public static boolean isRegularFile(@Nonnull String path, @Nonnull DirCache cache) {
    DirCacheEntry entry = getEntry(path, cache);
    return entry != null && entry.getFileMode() == FileMode.REGULAR_FILE;
  }

  public static boolean isExecutableFile(@Nonnull String path, @Nonnull DirCache cache) {
    DirCacheEntry entry = getEntry(path, cache);
    return entry != null && entry.getFileMode() == FileMode.EXECUTABLE_FILE;
  }

  public static boolean isRegularOrExecutableFile(@Nonnull String path, @Nonnull DirCache cache) {
    DirCacheEntry entry = getEntry(path, cache);
    return entry != null
             && (entry.getFileMode() == FileMode.REGULAR_FILE || entry.getFileMode() == FileMode.EXECUTABLE_FILE);
  }

  public static boolean isNonEmptyDirectory(@Nonnull String path, @Nonnull DirCache cache) {
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

  @Nonnull
  public static Iterator<CacheNode> iterateDirectory(@Nonnull String path, boolean recursive, @Nonnull DirCache cache) {
    path = TreeUtils.normalizeTreePath(path);
    DirCacheEntry[] entries = cache.getEntriesWithin(path);
    if(entries.length == 0)
      throw new NoSuchCacheDirectoryException("/" + path);
    return recursive ? new CacheIterator(entries) : new CacheIterator(entries, path);
  }

  @Nonnull
  public static Iterator<CacheNode> iterateDirectory(@Nonnull String path, @Nonnull DirCache cache) {
    return iterateDirectory(path, false, cache);
  }

  @Nonnull
  public static ObjectId writeTree(@Nonnull DirCache cache, @Nonnull Repository repo) throws IOException {
    try(ObjectInserter inserter = repo.newObjectInserter()) {
      ObjectId tree = cache.writeTree(inserter);
      inserter.flush();
      return tree;
    }
  }

  @Nonnull
  private static DirCacheEntry ensureEntry(@Nonnull String path, @Nonnull DirCache cache) {
    DirCacheEntry entry = getEntry(path, cache);
    if(entry == null)
      throw new NoSuchCacheEntryException("/" + TreeUtils.normalizeTreePath(path));
    return entry;
  }

}
