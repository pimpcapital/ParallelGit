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

import static com.beijunyi.parallelgit.utils.TreeUtils.normalizeNodePath;
import static org.eclipse.jgit.dircache.DirCacheEntry.STAGE_0;
import static org.eclipse.jgit.lib.Constants.encode;
import static org.eclipse.jgit.lib.FileMode.*;

public final class CacheUtils {

  @Nonnull
  public static DirCacheEntry newDirCacheEntry(String path) {
    return new DirCacheEntry(normalizeNodePath(path));
  }

  @Nonnull
  public static DirCacheEditor.DeletePath deleteEntry(String path) {
    return new DirCacheEditor.DeletePath(normalizeNodePath(path));
  }

  @Nonnull
  public static DirCacheEditor.DeleteTree deleteChildren(String path) {
    return new DirCacheEditor.DeleteTree(normalizeNodePath(path));
  }

  public static void loadTree(AnyObjectId treeId, DirCache cache, ObjectReader reader) throws IOException {
    addTree("", treeId, cache, reader);
  }

  public static void loadRevision(AnyObjectId revision, DirCache cache, ObjectReader reader) throws IOException {
    loadTree(CommitUtils.getCommit(revision, reader).getTree(), cache, reader);
  }

  @Nonnull
  public static DirCache forTree(AnyObjectId treeId, ObjectReader reader) throws IOException {
    DirCache cache = DirCache.newInCore();
    loadTree(treeId, cache, reader);
    return cache;
  }

  @Nonnull
  public static DirCache forTree(AnyObjectId treeId, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return forTree(treeId, reader);
    }
  }

  @Nonnull
  public static DirCache forRevision(AnyObjectId revision, ObjectReader reader) throws IOException {
    DirCache cache = DirCache.newInCore();
    loadRevision(revision, cache, reader);
    return cache;
  }

  @Nonnull
  public static DirCache forRevision(AnyObjectId revision, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return forRevision(revision, reader);
    }
  }

  @Nonnull
  public static DirCache forRevision(Ref revision, Repository repo) throws IOException {
    return forRevision(revision.getObjectId(), repo);
  }

  @Nonnull
  public static DirCache forRevision(String revision, Repository repo) throws IOException {
    AnyObjectId revisionId = repo.resolve(revision);
    if(revisionId == null)
      throw new NoSuchCommitException(revision);
    return forRevision(revisionId, repo);
  }

  @Nonnull
  public static DirCacheBuilder keepEverything(DirCache cache) {
    DirCacheBuilder builder = cache.builder();
    int count = cache.getEntryCount();
    if(count > 0)
      builder.keep(0, count);
    return builder;
  }

  public static void addTree(String path, AnyObjectId treeId, DirCacheBuilder builder, ObjectReader reader) throws IOException {
    builder.addTree(encode(normalizeNodePath(path)), STAGE_0, reader, treeId);
  }

  public static void addTree(String path, AnyObjectId treeId, DirCache cache, ObjectReader reader) throws IOException {
    DirCacheBuilder builder = keepEverything(cache);
    addTree(path, treeId, builder, reader);
    builder.finish();
  }

  public static void addFile(String path, FileMode mode, AnyObjectId blobId, DirCacheBuilder builder) {
    DirCacheEntry entry = newDirCacheEntry(path);
    entry.setFileMode(mode);
    entry.setObjectId(blobId);
    builder.add(entry);
  }

  public static void addFile(String path, FileMode mode, AnyObjectId blobId, DirCache cache) {
    DirCacheBuilder builder = keepEverything(cache);
    addFile(path, mode, blobId, builder);
    builder.finish();
  }

  public static void addFile(String path, AnyObjectId blobId, DirCache cache) {
    addFile(path, REGULAR_FILE, blobId, cache);
  }

  public static void deleteFile(String path, DirCacheEditor editor) {
    editor.add(deleteEntry(path));
  }

  public static void deleteFile(String path, DirCache cache) {
    DirCacheEditor editor = cache.editor();
    deleteFile(path, editor);
    editor.finish();
  }

  public static void deleteDirectory(String path, DirCacheEditor editor) {
    editor.add(deleteChildren(path));
  }

  public static void deleteDirectory(String path, DirCache cache) {
    DirCacheEditor editor = cache.editor();
    deleteDirectory(path, editor);
    editor.finish();
  }

  @Nullable
  public static DirCacheEntry getEntry(String path, DirCache cache) {
    return cache.getEntry(normalizeNodePath(path));
  }

  public static void updateFile(CacheEntryUpdate update, DirCacheEditor editor) {
    editor.add(update);
  }

  public static void updateFile(CacheEntryUpdate update, DirCache cache) {
    DirCacheEditor editor = cache.editor();
    updateFile(update, editor);
    editor.finish();
  }

  public static void updateFileBlob(String path, ObjectId blob, FileMode mode, DirCache cache) {
    updateFile(new CacheEntryUpdate(path).setNewBlob(blob).setNewFileMode(mode), cache);
  }

  public static void updateFileBlob(String path, ObjectId blob, DirCache cache) {
    updateFile(new CacheEntryUpdate(path).setNewBlob(blob), cache);
  }

  public static void updateFileMode(String path, FileMode mode, DirCache cache) {
    updateFile(new CacheEntryUpdate(path).setNewFileMode(mode), cache);
  }

  public static int findEntry(String path, DirCache cache) {
    return cache.findEntry(normalizeNodePath(path));
  }

  public static boolean entryExists(String path, DirCache cache) {
    return findEntry(path, cache) >= 0;
  }

  @Nonnull
  public static ObjectId getBlob(String path, DirCache cache) throws NoSuchCacheEntryException {
    return ensureEntry(path, cache).getObjectId();
  }

  @Nonnull
  public static FileMode getFileMode(String path, DirCache cache) throws NoSuchCacheEntryException {
    return ensureEntry(path, cache).getFileMode();
  }


  public static boolean isFile(String path, DirCache cache) {
    return entryExists(path, cache);
  }

  public static boolean isSymbolicLink(String path, DirCache cache) {
    DirCacheEntry entry = getEntry(path, cache);
    return entry != null && entry.getFileMode() == SYMLINK;
  }

  public static boolean isRegularFile(String path, DirCache cache) {
    DirCacheEntry entry = getEntry(path, cache);
    return entry != null && entry.getFileMode() == REGULAR_FILE;
  }

  public static boolean isExecutableFile(String path, DirCache cache) {
    DirCacheEntry entry = getEntry(path, cache);
    return entry != null && entry.getFileMode() == EXECUTABLE_FILE;
  }

  public static boolean isRegularOrExecutableFile(String path, DirCache cache) {
    DirCacheEntry entry = getEntry(path, cache);
    return entry != null
             && (entry.getFileMode() == REGULAR_FILE || entry.getFileMode() == EXECUTABLE_FILE);
  }

  public static boolean isNonEmptyDirectory(String path, DirCache cache) {
    path = normalizeNodePath(path) + "/";
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
  public static Iterator<CacheNode> iterateDirectory(String path, boolean recursive, DirCache cache) {
    path = normalizeNodePath(path);
    DirCacheEntry[] entries = cache.getEntriesWithin(path);
    if(entries.length == 0)
      throw new NoSuchCacheDirectoryException("/" + path);
    return recursive ? new CacheIterator(entries) : new CacheIterator(entries, path);
  }

  @Nonnull
  public static Iterator<CacheNode> iterateDirectory(String path, DirCache cache) {
    return iterateDirectory(path, false, cache);
  }

  @Nonnull
  public static ObjectId writeTree(DirCache cache, Repository repo) throws IOException {
    try(ObjectInserter inserter = repo.newObjectInserter()) {
      ObjectId tree = cache.writeTree(inserter);
      inserter.flush();
      return tree;
    }
  }

  @Nonnull
  private static DirCacheEntry ensureEntry(String path, DirCache cache) {
    DirCacheEntry entry = getEntry(path, cache);
    if(entry == null)
      throw new NoSuchCacheEntryException("/" + normalizeNodePath(path));
    return entry;
  }

}
