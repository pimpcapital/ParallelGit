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

public class GfsFileStore extends FileStore {

  private final DirectoryNode root;

  public GfsFileStore(@Nonnull AnyObjectId rootTree, @Nonnull GfsDataService dataService) throws IOException {
    this(DirectoryNode.forTreeObject(rootTree, dataService));
  }

  private GfsFileStore(@Nonnull DirectoryNode rootNode) {
    root = rootNode;
  }

  @Nonnull
  @Override
  public String name() {
    return "gfs";
  }

  @Nonnull
  @Override
  public String type() {
    return "gfs";
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
  public AnyObjectId persist() {
    return root.
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
