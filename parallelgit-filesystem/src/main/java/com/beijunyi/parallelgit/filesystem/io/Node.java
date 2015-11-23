package com.beijunyi.parallelgit.filesystem.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsDataService;
import com.beijunyi.parallelgit.utils.io.GitFileEntry;
import com.beijunyi.parallelgit.utils.io.ObjectSnapshot;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

import static org.eclipse.jgit.lib.FileMode.*;

public abstract class Node<Snapshot extends ObjectSnapshot> {

  protected final GfsDataService gds;

  protected volatile AnyObjectId id;
  protected volatile Snapshot snapshot;
  protected volatile boolean deleted = false;

  protected Node(@Nullable AnyObjectId id, @Nullable Snapshot snapshot, @Nonnull GfsDataService gds) {
    this.id = id;
    this.snapshot = snapshot;
    this.gds = gds;
  }

  @Nonnull
  public static Node fromEntry(@Nonnull GitFileEntry entry, @Nonnull GfsDataService gds) {

  }

  @Nonnull
  public static Node forObject(@Nonnull AnyObjectId object, @Nonnull FileMode mode, @Nonnull GfsDataService ds) {
    if(mode.equals(TREE))
      return DirectoryNode.forTreeObject(object, ds);
    return FileNode.forBlobId(object, mode, ds);
  }

  @Nonnull
  public static Node cloneNode(@Nonnull Node node, @Nonnull GfsDataService ds) {
    Node ret;
    if(node instanceof DirectoryNode)
      ret = DirectoryNode.newDirectory(ds);
    else
      ret = FileNode.newFile(node.isExecutableFile(), ds);
    return ret;
  }

  @Nonnull
  public GfsDataService getDataService() {
    return gds;
  }

  @Nonnull
  public FileMode getMode() {
    return mode;
  }

  public void setMode(@Nonnull FileMode mode) {
    this.mode = mode;
  }

  public boolean isRegularFile() {
    return mode.equals(REGULAR_FILE) || mode.equals(EXECUTABLE_FILE);
  }

  public boolean isExecutableFile() {
    return mode.equals(EXECUTABLE_FILE);
  }

  public boolean isSymbolicLink() {
    return mode.equals(FileMode.SYMLINK);
  }

  public boolean isDirectory() {
    return mode.equals(TREE);
  }

  @Nullable
  public AnyObjectId getObject() {
    return object;
  }

  public void setObject(@Nullable AnyObjectId object) {
    this.object = object;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public void reset(@Nonnull AnyObjectId newId, @Nonnull FileMode newMode) {
    if((mode.equals(TREE) || newMode.equals(TREE)) && !mode.equals(newMode))
      throw new IllegalStateException();
    reset();
    if(!newId.equals(object) || !newMode.equals(mode)) {
      setObject(newId);
      setMode(newMode);
    }
  }

  public void markDeleted() {
    setDeleted(true);
    reset();
  }

  public void takeSnapshot() {
  }

  public abstract boolean isInitialized();

  public boolean isDirty() {
    return false;
  }

  protected abstract boolean isTrivial();

  protected abstract void reset();

}
