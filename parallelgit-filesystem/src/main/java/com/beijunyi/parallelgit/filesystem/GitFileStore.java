package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.exceptions.NoTreeException;
import com.beijunyi.parallelgit.filesystem.io.DirectoryNode;
import com.beijunyi.parallelgit.filesystem.io.GfsFileAttributeView;
import org.eclipse.jgit.lib.AnyObjectId;

public class GitFileStore extends FileStore {

  private final String name;
  private final DirectoryNode root;

  public GitFileStore(@Nonnull String name, @Nullable AnyObjectId rootTree) throws IOException {
    this(name, rootTree != null ? DirectoryNode.forTreeObject(rootTree, null) : DirectoryNode.newDirectory(null));
  }

  private GitFileStore(@Nonnull String name, @Nonnull DirectoryNode rootNode) {
    this.name = name;
    root = rootNode;
  }

  @Nonnull
  @Override
  public String name() {
    return name;
  }

  @Nonnull
  @Override
  public String type() {
    return "gitfs";
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public long getTotalSpace() {
    return 0;
  }

  @Override
  public long getUsableSpace() {
    return Runtime.getRuntime().freeMemory();
  }

  @Override
  public long getUnallocatedSpace() {
    return 0;
  }

  @Override
  public boolean supportsFileAttributeView(@Nonnull Class<? extends FileAttributeView> type) {
    return type.isAssignableFrom(GfsFileAttributeView.Basic.class)
             || type.isAssignableFrom(GfsFileAttributeView.Posix.class) ;
  }

  @Override
  public boolean supportsFileAttributeView(@Nonnull String name) {
    switch(name) {
      case GfsFileAttributeView.Basic.BASIC_VIEW:
      case GfsFileAttributeView.Posix.POSIX_VIEW:
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
    AnyObjectId rootObject = root.getObject();
    if(rootObject == null)
      throw new NoTreeException();
    return rootObject;
  }

  public boolean isDirty() {
    return root.isDirty();
  }

}
