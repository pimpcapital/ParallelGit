package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.hierarchy.Node;
import com.beijunyi.parallelgit.filesystem.hierarchy.RootNode;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.treewalk.TreeWalk;

public class GitFileStore extends FileStore {

  private final Repository repository;
  private final GitPath rootPath;
  private ObjectReader reader;
  private ObjectInserter inserter;
  private RootNode root;


  GitFileStore(@Nonnull Repository repository, @Nonnull GitPath rootPath, @Nullable AnyObjectId baseTree) throws IOException {
    this.repository = repository;
    this.rootPath = rootPath;
    root = RootNode.newRoot(rootPath, baseTree, this);
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
    return repository.getDirectory().getAbsolutePath();
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
  public synchronized void release() throws IOException {
    root.lock();
    if(reader != null)
      reader.release();
    if(inserter != null)
      inserter.release();
  }

  @Nonnull
  private ObjectReader reader() {
    if(reader != null)
      return reader;
    synchronized(this) {
      if(reader == null)
        reader = repository.newObjectReader();
      return reader;
    }
  }

  @Nonnull
  private ObjectInserter inserter() {
    if(inserter != null)
      return inserter;
    synchronized(this) {
      if(inserter == null)
        inserter = repository.newObjectInserter();
      return inserter;
    }
  }

  public long getBlobSize(@Nonnull AnyObjectId blobId) throws IOException {
    return reader().getObjectSize(blobId, Constants.OBJ_BLOB);
  }

  @Nonnull
  public byte[] getBlobBytes(@Nonnull AnyObjectId blobId) throws IOException {
    return reader().open(blobId).getBytes();
  }

  @Nonnull
  public TreeWalk newTreeWalk() {
    return new TreeWalk(reader());
  }

  @Nonnull
  public AnyObjectId insertBlob(@Nonnull byte[] bytes) throws IOException {
    return inserter().insert(Constants.OBJ_BLOB, bytes);
  }

  @Nonnull
  public AnyObjectId insertTree(@Nonnull TreeFormatter tf) throws IOException {
    return inserter().insert(tf);
  }

  public boolean baseSameRepository(@Nonnull GitFileStore store) {
    return repository.getDirectory().equals(store.repository.getDirectory());
  }

  @Nonnull
  public Repository getRepository() {
    return repository;
  }

  @Nonnull
  public AnyObjectId getTree() {
    return root.getObject();
  }

  @Nullable
  public Node findNode(@Nonnull GitPath path) throws IOException {
    if(!path.isAbsolute())
      throw new IllegalArgumentException(path.toString());
    Node current = root;
    path = rootPath.relativize(path);
    for(int i = 0; i < path.getNameCount(); i++) {
      GitPath name = path.getName(i);
      current = current.asDirectory().getChild(name.toString());
      if(current == null)
        break;
    }
    return current;
  }

  @Nonnull
  public AnyObjectId persistChanges() throws IOException {
    AnyObjectId tree = root.save();
    inserter().flush();
    return tree;
  }

}
