package com.beijunyi.parallelgit.filesystem;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.io.*;
import com.beijunyi.parallelgit.utils.*;
import org.eclipse.jgit.dircache.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;

public class GitFileStore extends FileStore implements Closeable {

  public static final String ATTACHED = "attached";
  public static final String DETACHED = "detached";

  private final GitPath root;
  private final Repository repo;
  private final ObjectReader reader;

  private final Map<String, GitFileStoreMemoryChannel> memoryChannels = new ConcurrentHashMap<>();
  private final Map<String, Collection<GitDirectoryStream>> dirStreams = new ConcurrentHashMap<>();

  private final Map<String, ObjectId> insertions = new ConcurrentHashMap<>();
  private final Set<String> insertedDirs = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
  private final Map<String, FileMode> fileModes = new ConcurrentHashMap<>();
  private final Set<String> deletions = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
  private final Map<String, Integer> deletedDirs = new ConcurrentHashMap<>();

  private String branch;
  private RevCommit baseCommit;
  private AnyObjectId baseTree;

  private volatile DirCache cache;
  private volatile boolean closed = false;
  private volatile ObjectInserter inserter;



  GitFileStore(@Nonnull GitPath root, @Nonnull Repository repo, @Nullable String branchRef, @Nullable AnyObjectId basedRevision, @Nullable AnyObjectId baseTree) throws IOException {
    this.root = root;
    this.repo = repo;
    this.branch = branchRef;
    this.reader = repo.newObjectReader();

    if(basedRevision != null)
      baseCommit = CommitHelper.getCommit(reader, basedRevision);
    else
      cache = DirCache.newInCore();

    if(baseTree == null && baseCommit != null)
      this.baseTree = baseCommit.getTree();
    else
      this.baseTree = baseTree;
  }

  /**
   * Returns the name of this file store.
   *
   * A {@code GitFileStore}'s name consists of the absolute path to the repository directory, the branch ref, the base
   * commit id and the base tree id.
   *
   * @return the name of this file store
   */
  @Nonnull
  @Override
  public String name() {
    StringBuilder name = new StringBuilder(repo.getDirectory().getAbsolutePath());
    name.append(':');
    if(branch != null)
      name.append(branch);
    name.append(':');
    if(baseCommit != null)
      name.append(baseCommit.getName());
    name.append(':');
    if(baseTree != null)
      name.append(baseTree.getName());
    return name.toString();
  }

  /**
   * Returns the type of this file store.
   *
   * A {@code GitFileStore} is "attached" if it is created with a branch ref specified. Committing changes on an
   * attached {@code GitFileStore} updates the {@code HEAD} of the specified branch. Otherwise, if no branch ref is
   * specified, the type of such file store is "detached".
   *
   * @return  the type of this file store
   */
  @Nonnull
  @Override
  public String type() {
    return branch != null ? ATTACHED : DETACHED;
  }

  /**
   * Returns {@code false} as {@code GitFileStore} supports write access.
   *
   * @return   {@code false}
   */
  @Override
  public boolean isReadOnly() {
    return false;
  }

  /**
   * Returns the size, in bytes, of the file store.
   *
   * This method simply forwards the result of {@link java.io.File#getTotalSpace()} from the repository directory.
   *
   * @return the size of the file store, in bytes
   */
  @Override
  public long getTotalSpace() {
    return repo.getDirectory().getTotalSpace();
  }

  /**
   * Returns the number of bytes available to this file store.
   *
   * This method simply forwards the result of {@link java.io.File#getUsableSpace()} from the repository directory.
   *
   * @return the number of bytes available
   */
  @Override
  public long getUsableSpace() {
    return repo.getDirectory().getUsableSpace();
  }

  /**
   * Returns the number of unallocated bytes in the file store.
   *
   * This method simply forwards the result of {@link java.io.File#getFreeSpace()} from the repository directory.
   *
   * @return the number of unallocated bytes
   */
  @Override
  public long getUnallocatedSpace() {
    return repo.getDirectory().getFreeSpace();
  }

  @Override
  public boolean supportsFileAttributeView(@Nonnull Class<? extends FileAttributeView> type) {
    return type.isAssignableFrom(GitFileAttributeView.class);
  }

  @Override
  public boolean supportsFileAttributeView(@Nonnull String name) {
    switch(name) {
      case GitFileAttributeView.Basic.BASIC_VIEW:
      case GitFileAttributeView.Posix.POSIX_VIEW:
        return true;
      default:
        return false;
    }
  }

  @Nullable
  @Override
  public <V extends FileStoreAttributeView> V getFileStoreAttributeView(@Nonnull Class<V> type) {
    return null;
  }

  /**
   * Reads the value of a file store attribute.
   *
   * @param   attribute
   *          the attribute to read
   * @return  the attribute value
   */
  @Override
  public Object getAttribute(@Nonnull String attribute) {
    if(attribute.equals("totalSpace"))
      return getTotalSpace();
    if(attribute.equals("usableSpace"))
      return getUsableSpace();
    if(attribute.equals("unallocatedSpace"))
      return getUnallocatedSpace();
    throw new UnsupportedOperationException("'" + attribute + "' not recognized");
  }

  /**
   * Applies all the staged insertions to the cache.
   * If there is no insertion staged, this method has no effect.
   */
  private void applyInsertions() {
    if(!insertions.isEmpty()) {
      DirCacheBuilder builder = DirCacheHelper.keepEverything(cache);
      for(Map.Entry<String, ObjectId> entry : insertions.entrySet())
        DirCacheHelper.addFile(builder, fileModes.get(entry.getKey()), entry.getKey(), entry.getValue());
      builder.finish();
      insertions.clear();
      insertedDirs.clear();
      fileModes.clear();
    }
  }

  /**
   * Applies all the staged deletions to the cache.
   * If there is no deletion staged, this method has no effect.
   */
  private void applyDeletions() {
    if(!deletions.isEmpty()) {
      DirCacheEditor editor = cache.editor();
      for(String path : deletions)
        DirCacheHelper.deleteFile(editor, path);
      editor.finish();
      deletions.clear();
      deletedDirs.clear();
    }
  }

  /**
   * Applies all staged changes to the cache.
   * If there is no changes staged, this method has no effect.
   */
  private void flushStagedChanges() {
    applyInsertions();
    applyDeletions();
  }

  /**
   * Stages a file insertion with the specified path and blob.
   * This method flushes all the staged deletions before staging the insertion.
   *
   * @param   pathStr
   *          the string path to the file to insert
   * @param   blobId
   *          the id of the blob to insert
   */
  private void stageFileInsertion(@Nonnull String pathStr, @Nonnull ObjectId blobId) {
    applyDeletions();
    insertions.put(pathStr, blobId);
    fileModes.put(pathStr, FileMode.REGULAR_FILE);
    String current = pathStr;
    int sepIdx;
    while((sepIdx = current.lastIndexOf('/')) >= 0) {
      current = current.substring(0, sepIdx);
      if(!insertedDirs.add(current))
        break;
    }
  }

  private boolean isFileStagedForInsertion(@Nonnull String pathStr) {
    return insertions.containsKey(pathStr);
  }

  private boolean isDirectoryStagedForInsertion(@Nonnull String pathStr) {
    return insertedDirs.contains(pathStr);
  }

  /**
   * Stages a file deletion with the specified path.
   * This method flushes all the staged insertions before staging the deletion.
   *
   * @param   pathStr
   *          the string path to the file to delete
   */
  private void stageFileDeletion(@Nonnull String pathStr) {
    applyInsertions();
    deletions.add(pathStr);
    String current = pathStr;
    int sepIdx;
    while((sepIdx = current.lastIndexOf('/')) >= 0) {
      current = current.substring(0, sepIdx);
      if(!deletedDirs.containsKey(current)) {
        int children = cache.getEntriesWithin(current).length;
        deletedDirs.put(current, children);
      }
      int remain = deletedDirs.get(current);
      if(remain == 0)
        throw new IllegalStateException();
      deletedDirs.put(current, remain - 1);
    }
  }

  private boolean isFileStagedForDeletion(@Nonnull String pathStr) {
    return deletions.contains(pathStr);
  }

  private boolean isDirectoryStagedForDeletion(@Nonnull String pathStr) {
    return deletedDirs.containsKey(pathStr) && deletedDirs.get(pathStr) == 0;
  }

  private void clearMemoryChannels() {
    for(GitFileStoreMemoryChannel channel : memoryChannels.values())
      channel.close();
  }

  private void clearDirectoryChannels() {
    for(Collection<GitDirectoryStream> dirStreamsForPath : dirStreams.values())
      for(GitDirectoryStream directoryStream : dirStreamsForPath)
        directoryStream.close();
  }

  private void clearCache() {
    if(cache != null)
      cache.clear();
  }

  private void clearStore() {
    clearMemoryChannels();
    clearDirectoryChannels();
    clearCache();
  }

  private void releaseResources() {
    if(inserter != null)
      inserter.release();
    reader.release();
    repo.close();
  }

  /**
   * Closes this file store.
   *
   * After a file store is closed then all subsequent access to the file store, either by methods defined by this class
   * or on objects associated with this file store, throw {@link ClosedFileSystemException}. If the file store is
   * already closed then invoking this method has no effect.
   *
   * Closing a file store will close all open {@link java.nio.channels.Channel}, {@link DirectoryStream}, and other
   * closeable objects associated with this file store.
   */
  @Override
  public void close() {
    synchronized(this) {
      if(!closed) {
        closed = true;
        clearStore();
        releaseResources();
      }
    }
  }

  @Nonnull
  public GitPath getRoot() {
    return root;
  }

  /**
   * Returns the {@link Repository} this file store uses.
   *
   * @return the repository this file store uses
   */
  @Nonnull
  public Repository getRepository() {
    return repo;
  }

  /**
   * Returns an {@link ObjectInserter} of the {@link Repository} this file store uses.
   *
   * This method tries to reuse the instance from {@link #inserter}. If it does not already exist, a new inserter is
   * created by invoking {@link Repository#newObjectInserter()} from {@link #repo}.
   *
   * @return an object inserter of the repository this file store uses
   */
  @Nonnull
  private ObjectInserter getInserter() {
    if(inserter != null)
      return inserter;
    synchronized(this) {
      if(inserter == null)
        inserter = repo.newObjectInserter();
      return inserter;
    }
  }

  /**
   * Returns the branch this file store attaches to or {@code null} if this file store is detached.
   *
   * @return the branch this file store attaches to or {@code null} if this file store is detached
   */
  @Nullable
  public String getBranch() {
    return branch != null ? branch.substring(Constants.R_HEADS.length()) : null;
  }

  /**
   * Returns the {@link RevCommit} this file store bases on or {@code null} if there is no base commit.
   *
   * @return the commit this file store bases on or {@code null} if there is no base commit
   */
  @Nullable
  public RevCommit getBaseCommit() {
    return baseCommit;
  }

  /**
   * Returns the id of the tree this file store bases on or {@code null} if there is no base tree.
   *
   * @return the id of the tree this file store bases on or {@code null} if there is no base tree
   */
  @Nullable
  public AnyObjectId getBaseTree() {
    return baseTree;
  }

  /**
   * Tests if the file referenced by the specified path has been modified in this file store.
   *
   * @param pathStr a file path
   * @return {@code true} if the file has been modified
   */
  boolean isDirty(@Nonnull String pathStr) {
    checkClosed();
    synchronized(this) {
      GitFileStoreMemoryChannel channel = memoryChannels.get(pathStr);
      return channel != null && channel.isModified();
    }
  }

  /**
   * Returns the {@code ObjectId} of a file.
   *
   * @param   pathStr
   *          the string path to file whose {@code ObjectId} is to be returned
   * @return  the {@code ObjectId} of the file or {@code null} if it is a directory
   * @throws  NoSuchFileException
   *          if the specified file does not exist
   */
  @Nullable
  ObjectId getFileBlobId(@Nonnull String pathStr) throws IOException {
    checkClosed();
    synchronized(this) {
      if(isFileStagedForInsertion(pathStr))
        return insertions.get(pathStr);
      if(isDirectory(pathStr))
        return null;
      ObjectId result = cache != null ? DirCacheHelper.getBlobId(cache, pathStr) : TreeWalkHelper.getObject(reader, pathStr, baseTree);
      if(result == null)
        throw new NoSuchFileException(pathStr);
      return result;
    }
  }

  /**
   * Prepares to create a file at the specified path.
   *
   * @param   pathStr
   *          the string path to the file to create
   * @param   replaceExisting
   *          whether to replace the existing file
   * @return  {@code true} if no file exists at the specified path
   *
   * @throws  FileAlreadyExistsException
   *          if a file exists at the path and {@code replaceExisting} is {@code false}
   * @throws  DirectoryNotEmptyException
   *          if a directory exists at the specified path
   */
  private boolean prepareCreateFile(@Nonnull String pathStr, boolean replaceExisting) throws IOException {
    boolean isDirectory = isDirectory(pathStr);
    boolean isFile = !isDirectory && fileExists(pathStr);

    if(!replaceExisting) {
      if(isDirectory || isFile)
        throw new FileAlreadyExistsException(pathStr);
    } else if(isDirectory)
      throw new DirectoryNotEmptyException(pathStr);

    return !isFile;
  }

  /**
   * Removes a {@code GitFileStoreMemoryChannel}.
   * If there is no channel associated with the specified path, this method has no effect.
   *
   * @param   pathStr
   *          the string path to the channel to remove
   * @throws  AccessDeniedException
   *          if the channel is being used by a {@code GitSeekableByteChannel}
   */
  private void removeMemoryChannel(@Nonnull String pathStr) throws AccessDeniedException {
    GitFileStoreMemoryChannel channel = memoryChannels.get(pathStr);
    if(channel != null) {
      if(channel.countAttachedChannels() != 0)
        throw new AccessDeniedException(pathStr);
      memoryChannels.remove(pathStr);
    }
  }


  /**
   * Updates a file's {@code ObjectId}. If the target file does not already exist, this method creates a new file at the
   * given path.
   *
   * @param   pathStr
   *          the string path to the file to update
   * @param   objectId
   *          the new {@code ObjectId}
   */
  private void createFile(@Nonnull String pathStr, @Nonnull ObjectId objectId) {
    assert deletions == null || !deletions.contains(pathStr);
    DirCacheEntry entry = cache.getEntry(pathStr);
    if(entry != null)
      entry.setObjectId(objectId);
    else
      stageFileInsertion(pathStr, objectId);
  }

  /**
   * Creates a file with the given {@code ObjectId} or throws an exception if the file cannot be created safely.
   *
   * @param   pathStr
   *          the string path to the file to create
   * @param   blobId
   *          the {@code ObjectId}
   * @param   replaceExisting
   *          whether to replace the target file if it already exists
   * @throws  AccessDeniedException
   *          if the file is associated with an open {@code GitSeekableByteChannel} or if its parent directories are
   *          associated with an open {@code GitDirectoryStream}
   * @throws  FileAlreadyExistsException
   *          if the file already exists but {@code replaceExisting} is {@code false}
   * @throws  DirectoryNotEmptyException
   *          if {@code replaceExisting} is {@code true} but the file is a non-empty directory
   */
  private void safelyCreateFile(@Nonnull String pathStr, @Nonnull ObjectId blobId, boolean replaceExisting) throws IOException {
    if(!prepareCreateFile(pathStr, replaceExisting))
      removeMemoryChannel(pathStr);
    createFile(pathStr, blobId);
  }

  /**
   * Creates a file from a blob.
   *
   * @param   pathStr
   *          the string path to the file to create
   * @param   blobId
   *          the id to the blob
   * @param   replaceExisting
   *          whether to replace the existing file
   * @throws  AccessDeniedException
   *          if the path is associated with a channel
   * @throws  FileAlreadyExistsException
   *          if a file exists at the path and {@code replaceExisting} is {@code false}
   * @throws  DirectoryNotEmptyException
   *          if a directory exists at the specified path
   */
  void createFileFromBlob(@Nonnull String pathStr, @Nonnull ObjectId blobId, boolean replaceExisting) throws IOException {
    checkClosed();
    prepareCache();
    synchronized(this) {
      safelyCreateFile(pathStr, blobId, replaceExisting);
    }
  }

  boolean fileExists(@Nonnull String pathStr) throws IOException {
    checkClosed();
    synchronized(this) {
      if(pathStr.isEmpty())
        return false;
      if(isFileStagedForDeletion(pathStr))
        return false;
      if(isFileStagedForInsertion(pathStr))
        return true;
      if(cache != null)
        return DirCacheHelper.isFile(cache, pathStr);
      return TreeWalkHelper.isFileOrSymbolicLink(reader, pathStr, baseTree);
    }
  }

  /**
   * Tests if a file is a regular file.
   *
   * @param   pathStr
   *          the string path to the file to test
   * @return  {@code true} if the file is a regular file
   */
  boolean isRegularFile(@Nonnull String pathStr) throws IOException {
    checkClosed();
    synchronized(this) {
      if(pathStr.isEmpty())
        return false;
      if(isFileStagedForDeletion(pathStr))
        return false;
      if(isFileStagedForInsertion(pathStr)) {
        FileMode mode = fileModes.get(pathStr);
        return mode == FileMode.REGULAR_FILE || mode == FileMode.EXECUTABLE_FILE;
      }
      if(cache != null)
        return DirCacheHelper.isRegularOrExecutableFile(cache, pathStr);
      return TreeWalkHelper.isRegularOrExecutableFile(reader, pathStr, baseTree);
    }
  }

  /**
   * Tests if a file is a directory.
   *
   * @param   pathStr
   *          the string path to the file to test
   * @return  {@code true} if the file is a directory
   */
  boolean isDirectory(@Nonnull String pathStr) throws IOException {
    checkClosed();
    if(pathStr.isEmpty())
      return true;
    synchronized(this) {
      if(isDirectoryStagedForDeletion(pathStr))
        return false;
      if(isDirectoryStagedForInsertion(pathStr))
        return true;
      if(cache != null)
        return DirCacheHelper.isNonTrivialDirectory(cache, pathStr);
      return TreeWalkHelper.isDirectory(reader, pathStr, baseTree);
    }
  }

  /**
   * Tests if a file is executable.
   *
   * @param   pathStr
   *          the string path to the file to test
   * @return  {@code true} if the file is executable
   */
  boolean isExecutableFile(@Nonnull String pathStr) throws IOException {
    checkClosed();
    if(pathStr.isEmpty())
      return false;
    synchronized(this) {
      if(isDirectoryStagedForDeletion(pathStr))
        return false;
      if(isDirectoryStagedForInsertion(pathStr))
        return fileModes.get(pathStr) == FileMode.EXECUTABLE_FILE;
      if(cache != null) {
        return DirCacheHelper.isExecutableFile(cache, pathStr);
      }
      return TreeWalkHelper.isExecutableFile(reader, pathStr, baseTree);
    }
  }

  /**
   * Tests if a file is a symbolic link.
   *
   * @param   pathStr
   *          the string path to the file to test
   * @return  {@code true} if the file is a symbolic link
   */
  boolean isSymbolicLink(@Nonnull String pathStr) throws IOException {
    checkClosed();
    if(pathStr.isEmpty())
      return false;
    synchronized(this) {
      if(isDirectoryStagedForDeletion(pathStr))
        return false;
      if(isDirectoryStagedForInsertion(pathStr))
        return fileModes.get(pathStr) == FileMode.SYMLINK;
      if(cache != null) {
        return DirCacheHelper.isSymbolicLink(cache, pathStr);
      }
      return TreeWalkHelper.isSymbolicLink(reader, pathStr, baseTree);
    }
  }

  /**
   * Returns a file's size in bytes. A non-empty directory is considered to have 0 byte.
   *
   * @param   pathStr
   *          the path to the file
   * @return  the file's size in bytes
   *
   * @throws  NoSuchFileException
   *          if the file does not exist
   */
  long getFileSize(@Nonnull String pathStr) throws IOException {
    checkClosed();
    synchronized(this) {
      GitFileStoreMemoryChannel channel = memoryChannels.get(pathStr);
      if(channel != null) {
        channel.lockBuffer();
        try {
          return channel.size();
        } finally {
          channel.releaseBuffer();
        }
      }
      ObjectId blobId = getFileBlobId(pathStr);
      if(blobId == null)
        return 0;
      return reader.getObjectSize(blobId, Constants.OBJ_BLOB);
    }
  }

  /**
   * Prepares {@link #cache} by loading {@link #baseTree}.
   * If the cache is already available, this method has no effect.
   */
  void prepareCache() throws IOException {
    checkClosed();
    if(cache != null)
      return;
    synchronized(this) {
      if(cache == null) {
        assert baseTree != null;
        cache = DirCacheHelper.forTree(reader, baseTree);
      }
    }
  }

  /**
   * Deletes a regular file. This method does not check if the file's parent directories are associated with an open
   * {@code GitDirectoryStream}. It is up to the caller to decide whether such check is necessary.
   *
   * @param   pathStr
   *          the string path to the file to delete
   * @throws  AccessDeniedException
   *          if the file is associated with an open {@code GitSeekableByteChannel}
   */
  private void deleteFile(@Nonnull String pathStr) throws AccessDeniedException {
    GitFileStoreMemoryChannel memoryChannel = memoryChannels.get(pathStr);
    if(memoryChannel != null) {
      if(memoryChannel.countAttachedChannels() != 0)  // if there is an attached channel
        throw new AccessDeniedException(pathStr);  // fail the deletion
      memoryChannel.close();
      memoryChannels.remove(pathStr);
    }
    stageFileDeletion(pathStr);
  }

  /**
   * Deletes a file.
   *
   * @param   pathStr
   *          the string path to the file to delete
   * @throws  AccessDeniedException
   *          if the file is associated with an open {@code GitSeekableByteChannel} or if its parent directories are
   *          associated with an open {@code GitDirectoryStream}
   * @throws  DirectoryNotEmptyException
   *          if the file is a non-empty directory
   * @throws  NoSuchFileException
   *          if the file does not exist
   */
  void delete(@Nonnull String pathStr) throws IOException {
    checkClosed();
    prepareCache();
    synchronized(this) {
      if(fileExists(pathStr))
        deleteFile(pathStr);
      else if(isDirectory(pathStr))
        throw new DirectoryNotEmptyException(pathStr);
      else
        throw new NoSuchFileException(pathStr);
    }
  }

  public void deleteDirectory(@Nonnull String pathStr) throws IOException {
    prepareCache();
    synchronized(this) {
      flushStagedChanges();
      DirCacheHelper.deleteDirectory(cache, pathStr);
    }
  }

  /**
   * Clones a {@code GitFileStoreMemoryChannel} for the target path from the source channel. The result channel's buffer
   * has the same content as the source channel.
   *
   * @param   sourceStr
   *          the string path to the {@code GitFileStoreMemoryChannel} to clone
   * @param   targetStr
   *          the string path to the result {@code GitFileStoreMemoryChannel}
   */
  private void cloneChannel(@Nonnull String sourceStr, @Nonnull String targetStr) {
    GitFileStoreMemoryChannel sourceChannel = memoryChannels.get(sourceStr);
    if(sourceChannel != null) {
      sourceChannel.lockBuffer();
      try {
        byte[] bytes = Arrays.copyOf(sourceChannel.getBytes(), (int)sourceChannel.size());
        GitFileStoreMemoryChannel targetChannel = new GitFileStoreMemoryChannel(this, targetStr, bytes);
        memoryChannels.put(targetStr, targetChannel);
      } finally {
        sourceChannel.releaseBuffer();
      }
    }
  }

  /**
   * Copy a file to a target file.
   *
   * @param   sourceStr
   *          the string path to the file to copy
   * @param   targetStr
   *          the string path to the target file
   * @param   replaceExisting
   *          whether to replace the target file if it already exists
   * @throws  AccessDeniedException
   *          if the file to copy or the target file is associated with an open {@code GitSeekableByteChannel} or their
   *          parent directories are associated with an open {@code GitDirectoryStream}
   * @throws  NoSuchFileException
   *          if the file to copy does not exist
   * @throws  FileAlreadyExistsException
   *          if the target file exists but cannot be replaced because {@code replaceExisting} is {@code false}
   * @throws  DirectoryNotEmptyException
   *          if {@code replaceExisting} is {@code true} but the file cannot be replaced because it is a non-empty
   *          directory
   */
  void copy(@Nonnull String sourceStr, @Nonnull String targetStr, boolean replaceExisting) throws IOException {
    checkClosed();
    prepareCache();
    if(targetStr.equals(sourceStr))
      return;
    synchronized(this) {
      ObjectId blobId = getFileBlobId(sourceStr);
      if(blobId == null)
        return;
      safelyCreateFile(targetStr, blobId, replaceExisting);
      cloneChannel(sourceStr, targetStr);
    }
  }

  /**
   * Move or rename a file to a target file.
   *
   * @param   sourceStr
   *          the string path to the file to move.
   * @param   targetStr
   *          the string path to the target file.
   * @param   replaceExisting
   *          whether to replace the target file if it already exists.
   * @throws  NoSuchFileException
   *          If the file to move does not exist.
   * @throws  AccessDeniedException
   *          if the file to move or the target file is associated with an open {@code GitSeekableByteChannel} or if
   *          their parent directories are associated with an open open {@code GitDirectoryStream}
   * @throws  FileAlreadyExistsException
   *          If the target file already exists but {@code replaceExisting} is set to {@code false}.
   * @throws  DirectoryNotEmptyException
   *          If {@code replaceExisting} is set to {@code true} but the target file is a directory.
   */
  void move(@Nonnull String sourceStr, @Nonnull String targetStr, boolean replaceExisting) throws IOException {
    checkClosed();
    prepareCache();
    synchronized(this) {
      // get the blob or throw exception if file does not exist
      ObjectId sourceBlob = getFileBlobId(sourceStr);

      if(targetStr.equals(sourceStr))
        return;

      if(targetStr.startsWith(sourceStr + "/"))
        throw new AccessDeniedException(targetStr);

      boolean targetExists = !prepareCreateFile(targetStr, replaceExisting);
      if(targetExists)
        removeMemoryChannel(targetStr);

      if(sourceBlob != null) {
        createFile(targetStr, sourceBlob);
        cloneChannel(sourceStr, targetStr);
        deleteFile(sourceStr);
      } else {
        if(targetExists)
          deleteFile(targetStr);
        flushStagedChanges();
        DirCacheEntry[] childrenEntries = cache.getEntriesWithin(sourceStr);

        if(!sourceStr.endsWith("/"))
          sourceStr += "/";
        if(!targetStr.endsWith("/"))
          targetStr += "/";

        int sourceStrLen = sourceStr.length();

        Set<String> oldPaths = new HashSet<>();

        for(DirCacheEntry entry : childrenEntries) {
          String relativePath = entry.getPathString().substring(sourceStrLen);
          String fullPath = targetStr + relativePath;
          stageFileInsertion(fullPath, entry.getObjectId());
          cloneChannel(entry.getPathString(), fullPath);
          oldPaths.add(sourceStr + relativePath);
        }

        for(String oldPath : oldPaths)
          deleteFile(oldPath);
      }
    }
  }

  /**
   * Opens or creates a file, returning a {@code GitSeekableByteChannel} to access the file. This method works in
   * exactly the manner specified by the {@link Files#newByteChannel(Path,Set, java.nio.file.attribute.FileAttribute[])} method.
   *
   * @param   pathStr
   *          the string path to the file to open or create
   * @param   options
   *          options specifying how the file is opened
   *
   * @return  a {@code GitSeekableByteChannel} to access the target file
   *
   * @throws  NoSuchFileException
   *          if the file does not exists and neither {@link StandardOpenOption#CREATE} nor {@link
   *          StandardOpenOption#CREATE_NEW} option is specified
   * @throws  AccessDeniedException
   *          if the file is a directory
   * @throws  FileAlreadyExistsException
   *          if the file already exists and {@link StandardOpenOption#CREATE_NEW} option is specified
   */
  @Nonnull
  GitSeekableByteChannel newByteChannel(@Nonnull String pathStr, @Nonnull Set<OpenOption> options) throws IOException {
    checkClosed();
    options = Collections.unmodifiableSet(options);
    boolean readOnly = options.size() == 1 && options.contains(StandardOpenOption.READ);
    if(!readOnly)
      prepareCache();
    synchronized(this) {
      GitFileStoreMemoryChannel memoryChannel = memoryChannels.get(pathStr);
      if(memoryChannel != null) {
        if(options.contains(StandardOpenOption.CREATE_NEW))
          throw new FileAlreadyExistsException(pathStr);
      } else {
        ObjectId blobId;
        if(readOnly && cache == null) {
          if(pathStr.isEmpty())
            throw new AccessDeniedException(pathStr);
          TreeWalk treeWalk = TreeWalk.forPath(reader, pathStr, baseTree);
          if(treeWalk == null)
            throw new NoSuchFileException(pathStr);
          if(TreeWalkHelper.isTree(treeWalk))
            throw new AccessDeniedException(pathStr);
          blobId = treeWalk.getObjectId(0);
        } else {
          prepareCache();
          if(isDirectory(pathStr))
            throw new AccessDeniedException(pathStr);
          else if(!fileExists(pathStr)) {
            if(!options.contains(StandardOpenOption.CREATE) && !options.contains(StandardOpenOption.CREATE_NEW))
              throw new NoSuchFileException(pathStr);
            blobId = ObjectId.zeroId();
            stageFileInsertion(pathStr, blobId);
          } else if(options.contains(StandardOpenOption.CREATE_NEW))
            throw new FileAlreadyExistsException(pathStr);
          blobId = getFileBlobId(pathStr);
          assert blobId != null;
        }
        if(ObjectId.zeroId().equals(blobId))
          memoryChannel = new GitFileStoreMemoryChannel(this, pathStr);
        else
          memoryChannel = new GitFileStoreMemoryChannel(this, pathStr, reader.open(blobId).getBytes());
        memoryChannels.put(pathStr, memoryChannel);
      }
      return new GitSeekableByteChannel(memoryChannel, options);
    }
  }

  /**
   * Tries to remove the given {@code GitFileStoreMemoryChannel}. This method only removes a channel if it is not {@link
   * GitFileStoreMemoryChannel#isModified() modified} and not {@link GitFileStoreMemoryChannel#countAttachedChannels
   * attached} by any {@code GitSeekableByteChannel}.
   *
   * @param   channel
   *          the {@code GitFileStoreMemoryChannel} to remove
   */
  public void removeChannel(@Nonnull GitFileStoreMemoryChannel channel) {
    memoryChannels.remove(channel.getPathStr());
  }

  @Nonnull
  private GitDirectoryStream createDirectoryStream(@Nonnull String pathStr, @Nullable DirectoryStream.Filter<? super Path> filter) throws IOException {
    if(cache != null) {
      flushStagedChanges();
      return new DirCacheGitDirectoryStream(pathStr, this, cache, filter);
    }
    return new TreeWalkGitDirectoryStream(pathStr, this, reader, baseTree, filter);
  }

  private void registerDirectoryStream(@Nonnull GitDirectoryStream dirStream) {
    String pathStr = dirStream.getPathStr();
    synchronized(dirStreams) {
      Collection<GitDirectoryStream> streamsForPath = dirStreams.get(pathStr);
      if(streamsForPath == null) {
        streamsForPath = new ConcurrentLinkedQueue<>();
        dirStreams.put(pathStr, streamsForPath);
      }
      streamsForPath.add(dirStream);
    }
  }

  /**
   * Opens a directory, returning a {@code GitDirectoryStream} to iterate over the entries in the directory.
   *
   * @param   pathStr
   *          the string path to the directory
   * @param   filter
   *          the directory stream filter
   *
   * @return  a new and open {@code GitDirectoryStream} object
   *
   * @throws  NotDirectoryException
   *          if the file could not otherwise be opened because it is not a directory
   */
  @Nonnull
  GitDirectoryStream newDirectoryStream(@Nonnull String pathStr, @Nullable DirectoryStream.Filter<? super Path> filter) throws IOException {
    checkClosed();
    synchronized(this) {
      GitDirectoryStream dirStream = createDirectoryStream(pathStr, filter);
      registerDirectoryStream(dirStream);
      return dirStream;
    }
  }

  /**
   * Removes the reference to the given {@code DirectoryStream} from this {@code FileStore}.
   *
   * @param   dirStream
   *          the {@code DirectoryStream} to be removed
   */
  public void removeDirectoryStream(@Nonnull GitDirectoryStream dirStream) {
    String pathStr = dirStream.getPathStr();
    Collection<GitDirectoryStream> streamsForPath = dirStreams.get(pathStr);
    if(streamsForPath == null || !streamsForPath.remove(dirStream))
      throw new IllegalArgumentException("Could not find directory stream for " + pathStr);
    if(streamsForPath.isEmpty())
      dirStreams.remove(pathStr);
  }

  /**
   * Writes the cached files into the repository creating a new tree and updates the {@link #baseTree} of this store to
   * the root of the new tree. In the case that no file has been changed or the new tree is exactly the same as the
   * current tree, the {@link #baseTree} value will not be changed and {@code null} will be returned.
   *
   * @return  the {@code ObjectId} of the new tree or {@code null} if no new tree is created
   */
  @Nullable
  public ObjectId persistChanges() throws IOException {
    checkClosed();
    if(cache == null)
      return null;
    synchronized(this) {
      flushStagedChanges();

      // iterate through the memory channels and flush the byte array into the repository
      Iterator<Map.Entry<String, GitFileStoreMemoryChannel>> channelsIt = memoryChannels.entrySet().iterator();
      while(channelsIt.hasNext()) {
        GitFileStoreMemoryChannel channel = channelsIt.next().getValue();
        try {
          if(!channel.isModified())
            continue;
          channel.lockBuffer();
          byte[] blob = channel.getBytes();
          ObjectId blobId = getInserter().insert(Constants.OBJ_BLOB, blob);
          cache.getEntry(channel.getPathStr()).setObjectId(blobId);
          // if nothing relies on this channel
          if(channel.countAttachedChannels() == 0) {
            channel.close();
            channelsIt.remove();
          } else {
            // reset its modified flag to false, since it is already consistent with the repository
            channel.setModified(false);
          }
        } finally {
          channel.releaseBuffer();
        }
      }

      ObjectId newTreeId = cache.writeTree(getInserter());
      if(newTreeId.equals(baseTree))
        return null;

      baseTree = newTreeId;
      return newTreeId;
    }
  }

  /**
   * Writes the cached files into the repository creating a new commit and update the {@link #baseCommit} of this store
   * to the new commit. This method relies on {@link #persistChanges()} to create a new tree from the cache. In the
   * case that no new tree is created, the {@link #baseCommit} value will not be changed, and {@code null} will be
   * returned.
   *
   * @return  the new {@code RevCommit} or {@code null} if no new commit is created
   */
  @Nullable
  public RevCommit writeCommit(@Nullable PersonIdent author, @Nullable PersonIdent committer, @Nullable String message, boolean amend) throws IOException {
    checkClosed();
    synchronized(this) {
      AnyObjectId commitTree = persistChanges();
      if(commitTree == null && !amend)
        return null;

      List<AnyObjectId> parents = new ArrayList<>();
      if(amend) {
        if(baseCommit == null)
          throw new IllegalArgumentException("Could not amend without base commit");
        if(commitTree == null)
          commitTree = baseTree;
        if(author == null)
          author = baseCommit.getAuthorIdent();
        if(committer == null)
          committer = baseCommit.getCommitterIdent();
        if(message == null)
          message = baseCommit.getFullMessage();
        for(RevCommit p : baseCommit.getParents())
          parents.add(p.getId());
      } else if(baseCommit != null)
        parents.add(baseCommit);

      if(author == null)
        throw new IllegalArgumentException("Missing author");
      if(committer == null)
        throw new IllegalArgumentException("Missing committer");

      ObjectId newCommitId = CommitHelper.createCommit(getInserter(), commitTree, author, committer, message, parents);
      getInserter().flush();

      RevCommit newCommit = CommitHelper.getCommit(reader, newCommitId);
      if(branch != null) {
        if(baseCommit == null)
          BranchHelper.initBranchHead(repo, branch, newCommit, newCommit.getShortMessage());
        else if(amend)
          BranchHelper.amendBranchHead(repo, branch, newCommit, newCommit.getShortMessage());
        else
          BranchHelper.commitBranchHead(repo, branch, newCommit, newCommit.getShortMessage());
      }
      baseCommit = newCommit;

      return baseCommit;
    }
  }

  /**
   * Checks if this {@code GitFileStore} is closed.
   *
   * @throws  ClosedFileSystemException
   *          if this {@code GitFileStore} is closed
   */
  private void checkClosed() throws ClosedFileSystemException {
    if(closed)
      throw new ClosedFileSystemException();
  }

}
