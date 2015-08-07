package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.hierarchy.DirectoryNode;
import org.eclipse.jgit.lib.AnyObjectId;

public class GitFileStore extends FileStore {

  private final GitFileSystem gfs;
  private DirectoryNode root;


  GitFileStore(@Nonnull GitFileSystem gfs, @Nullable AnyObjectId rootTree) throws IOException {
    this.gfs = gfs;
    root = rootTree != null ? DirectoryNode.forTreeObject(rootTree) : DirectoryNode.newDirectory();
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
    return gfs.getSessionId();
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

  @Nonnull
  public DirectoryNode getRoot() {
    return root;
  }

  @Nonnull
  public AnyObjectId getTree() {
    return root.getObject();
  }

}
