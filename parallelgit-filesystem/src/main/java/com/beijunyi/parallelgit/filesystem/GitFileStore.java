package com.beijunyi.parallelgit.filesystem;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.hierarchy.DirectoryNode;
import com.beijunyi.parallelgit.filesystem.hierarchy.FileNode;
import com.beijunyi.parallelgit.filesystem.hierarchy.Node;
import com.beijunyi.parallelgit.filesystem.io.GitDirectoryStream;
import com.beijunyi.parallelgit.filesystem.io.GitSeekableByteChannel;
import com.beijunyi.parallelgit.utils.BranchHelper;
import com.beijunyi.parallelgit.utils.CommitHelper;
import com.beijunyi.parallelgit.utils.TreeWalkHelper;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;

public class GitFileStore extends FileStore implements Closeable {

  private DirectoryNode root;
  private volatile boolean closed = false;


  GitFileStore(@Nonnull ObjectReader reader, @Nonnull GitPath rootPath, @Nullable AnyObjectId baseTree) throws IOException {
    root = DirectoryNode.newRoot(rootPath, baseTree, reader);
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
    return root.getObject().getName();
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
    return "gitfs";
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
  public long getTotalSpace() throws IOException {
    return root.getSize();
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
    return Runtime.getRuntime().freeMemory();
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
    return 0;
  }

  @Override
  public boolean supportsFileAttributeView(@Nonnull Class<? extends FileAttributeView> type) {
    return type.isAssignableFrom(GitFileAttributeView.Basic.class);
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
  public Object getAttribute(@Nonnull String attribute) throws IOException {
    if(attribute.equals("totalSpace"))
      return getTotalSpace();
    if(attribute.equals("usableSpace"))
      return getUsableSpace();
    if(attribute.equals("unallocatedSpace"))
      return getUnallocatedSpace();
    throw new UnsupportedOperationException("'" + attribute + "' not recognized");
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
  public synchronized void close() throws IOException {
    if(!closed) {
      root.lock();
      closed = true;
    }
  }

  @Nonnull
  private Node getNode(@Nonnull GitPath path) throws IOException {
    Node node = root.findNode(path);
    if(node == null)
      throw new NoSuchFileException(path.toString());
    return node;
  }

  @Nonnull
  private DirectoryNode getDirectoryNode(@Nonnull GitPath path) throws IOException {
    Node node = getNode(path);
    if(!node.isDirectory())
      throw new NotDirectoryException(path.toString());
    return node.asDirectory();
  }

  @Nonnull
  private DirectoryNode getParentDirectory(@Nonnull GitPath path) throws IOException {
    GitPath parentPath = path.getParent();
    if(parentPath == null)
      throw new AccessDeniedException(path.toString());
    return getDirectoryNode(parentPath);
  }

  @Nonnull
  private String getFileName(@Nonnull GitPath path) {
    GitPath fileName = path.getFileName();
    if(fileName == null || fileName.isEmpty())
      throw new IllegalStateException();
    return fileName.toString();
  }

  /**
   * Tests if the file referenced by the specified path has been modified in this file store.
   *
   * @param path a file path
   * @return {@code true} if the file has been modified
   */
  public boolean isDirty(@Nonnull GitPath path) throws IOException {
    checkClosed();
    return getNode(path).isDirty();
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
  AnyObjectId getFileBlobId(@Nonnull String pathStr) throws IOException {
    checkClosed();
    Node node = root.findNode(pathStr);
    if(node == null)
      throw new NoSuchFileException(pathStr);
    return node instanceof FileNode ? node.getObject() : null;
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
    synchronized(this) {
      safelyCreateFile(pathStr, blobId, replaceExisting);
    }
  }

  public boolean fileExists(@Nonnull GitPath path) throws IOException {
    checkClosed();
    return root.findNode(path) != null;
  }

  /**
   * Tests if a file is a regular file.
   *
   * @param   path
   *          the path to the file to test
   * @return  {@code true} if the file is a regular file
   */
  public boolean isRegularFile(@Nonnull GitPath path) throws IOException {
    checkClosed();
    Node node = root.findNode(path);
    return node != null && node.isRegularFile();
  }

  /**
   * Tests if a file is a directory.
   *
   * @param   path
   *          the path to the file to test
   * @return  {@code true} if the file is a directory
   */
  public boolean isDirectory(@Nonnull GitPath path) throws IOException {
    checkClosed();
    Node node = root.findNode(path);
    return node != null && node.isDirectory();
  }

  /**
   * Tests if a file is executable.
   *
   * @param   path
   *          the string path to the file to test
   * @return  {@code true} if the file is executable
   */
  public boolean isExecutableFile(@Nonnull GitPath path) throws IOException {
    checkClosed();
    Node node = root.findNode(path);
    return node != null && node.isExecutableFile();
  }

  /**
   * Tests if a file is a symbolic link.
   *
   * @param   path
   *          the string path to the file to test
   * @return  {@code true} if the file is a symbolic link
   */
  public boolean isSymbolicLink(@Nonnull GitPath path) throws IOException {
    checkClosed();
    Node node = root.findNode(path);
    return node != null && node.isSymbolicLink();
  }

  /**
   * Returns a file's size in bytes. A non-empty directory is considered to have 0 byte.
   *
   * @param   path
   *          the path to the file
   * @return  the file's size in bytes
   *
   * @throws  NoSuchFileException
   *          if the file does not exist
   */
  public long getFileSize(@Nonnull GitPath path) throws IOException {
    checkClosed();
    return getNode(path).getSize();
  }

  /**
   * Deletes a file.
   *
   * @param   path
   *          the path to the file to delete
   * @throws  AccessDeniedException
   *          if the file is associated with an open {@code GitSeekableByteChannel} or if its parent directories are
   *          associated with an open {@code GitDirectoryStream}
   * @throws  DirectoryNotEmptyException
   *          if the file is a non-empty directory
   * @throws  NoSuchFileException
   *          if the file does not exist
   */
  public void delete(@Nonnull GitPath path) throws IOException {
    checkClosed();
    Node node = getNode(path);
    if(node.isDirectory() && !node.asDirectory().isEmpty())
      throw new DirectoryNotEmptyException(path.toString());
    node.delete();
  }

  public void deleteRecursively(@Nonnull GitPath path) throws IOException {
    checkClosed();
    getNode(path).delete();
  }

  /**
   * Copy a file to a target file.
   *
   * @param   source
   *          the path to the file to copy
   * @param   target
   *          the path to the target file
   * @param   replace
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
  public void copy(@Nonnull GitPath source, @Nonnull GitPath target, boolean replace) throws IOException {
    checkClosed();
    Node sourceNode = getNode(source);
    DirectoryNode parent = getParentDirectory(target);
    String childName = getFileName(target);
    if(sourceNode.isDirectory())
      parent.addChild(childName, DirectoryNode.newDirectory(), replace);
    else
      sourceNode.copyTo(parent, childName, replace);
  }

  public void copyRecursively(@Nonnull GitPath source, @Nonnull GitPath target, boolean replace) throws IOException {
    checkClosed();
    Node sourceNode = getNode(source);
    DirectoryNode parent = getParentDirectory(target);
    String childName = getFileName(target);
    sourceNode.copyTo(parent, childName, replace);
  }

  /**
   * Move or rename a file to a target file.
   *
   * @param   source
   *          the path to the file to move.
   * @param   target
   *          the path to the target file.
   * @param   replace
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
  public void move(@Nonnull GitPath source, @Nonnull GitPath target, boolean replace) throws IOException {
    checkClosed();
    DirectoryNode parent = getParentDirectory(target);
    String childName = getFileName(target);
    getNode(source).moveTo(parent, childName, replace);
  }

  /**
   * Opens or creates a file, returning a {@code GitSeekableByteChannel} to access the file. This method works in
   * exactly the manner specified by the {@link Files#newByteChannel(Path,Set, java.nio.file.attribute.FileAttribute[])} method.
   *
   * @param   path
   *          the path to the file to open or create
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
  GitSeekableByteChannel newByteChannel(@Nonnull GitPath path, @Nonnull Set<OpenOption> options) throws IOException {
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
      return null;
    }
  }

  /**
   * Opens a directory, returning a {@code GitDirectoryStream} to iterate over the entries in the directory.
   *
   * @param   path
   *          the path to the directory
   * @param   filter
   *          the directory stream filter
   *
   * @return  a new and open {@code GitDirectoryStream} object
   *
   * @throws  NotDirectoryException
   *          if the file could not otherwise be opened because it is not a directory
   */
  @Nonnull
  GitDirectoryStream newDirectoryStream(@Nonnull GitPath path, @Nullable DirectoryStream.Filter<? super Path> filter) throws IOException {
    checkClosed();
    DirectoryNode node = getDirectoryNode(path);
    return node.newStream(filter);
  }

  public void createDirectory(@Nonnull GitPath path) throws IOException {
    checkClosed();
    DirectoryNode parent = getParentDirectory(path);
    String childName = getFileName(path);
    parent.addChild(childName, DirectoryNode.newDirectory(), false);
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
