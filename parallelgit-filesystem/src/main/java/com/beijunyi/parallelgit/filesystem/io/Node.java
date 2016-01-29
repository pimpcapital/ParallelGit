package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsObjectService;
import com.beijunyi.parallelgit.utils.io.GitFileEntry;
import com.beijunyi.parallelgit.utils.io.ObjectSnapshot;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

import static org.eclipse.jgit.lib.FileMode.*;

public abstract class Node<Snapshot extends ObjectSnapshot> {

  protected final GfsObjectService objService;

  protected volatile DirectoryNode parent;
  protected volatile AnyObjectId originId;
  protected volatile AnyObjectId id;

  protected Node(@Nullable AnyObjectId id, @Nonnull GfsObjectService objService) {
    this.originId = id;
    this.id = id;
    this.parent = null;
    this.objService = objService;
  }

  protected Node(@Nullable AnyObjectId id, @Nonnull DirectoryNode parent) {
    this.originId = id;
    this.id = id;
    this.parent = parent;
    this.objService = parent.getObjService();
  }

  @Nonnull
  public static Node fromEntry(@Nonnull GitFileEntry entry, @Nonnull DirectoryNode parent) {
    if(entry.getMode().equals(TREE))
      return DirectoryNode.fromObject(entry.getId(), parent);
    return FileNode.fromObject(entry.getId(), entry.getMode(), parent);
  }

  @Nonnull
  public GfsObjectService getObjService() {
    return objService;
  }

  public boolean isRegularFile() {
    return getMode().equals(REGULAR_FILE) || getMode().equals(EXECUTABLE_FILE);
  }

  public boolean isExecutableFile() {
    return getMode().equals(EXECUTABLE_FILE);
  }

  public boolean isSymbolicLink() {
    return getMode().equals(SYMLINK);
  }

  public boolean isDirectory() {
    return getMode().equals(TREE);
  }

  @Nullable
  public AnyObjectId getObjectId(boolean persist) throws IOException {
    if(id == null || persist && !objService.hasObject(id)) {
      Snapshot snapshot = takeSnapshot(persist);
      id = snapshot != null ? snapshot.getId() : null;
    }
    if(persist)
      originId = id;
    return id;
  }

  public boolean isDirty() throws IOException {
    if(originId != null)
      return !originId.equals(getObjectId(false));
    return getObjectId(false) != null;
  }

  public abstract long getSize() throws IOException;

  @Nonnull
  public abstract FileMode getMode();

  public abstract void setMode(@Nonnull FileMode mode);

  public abstract void reset(@Nonnull AnyObjectId id);

  @Nullable
  public abstract Snapshot getSnapshot(boolean persist) throws IOException;

  public abstract boolean isInitialized();

  @Nonnull
  public abstract Node clone(@Nonnull DirectoryNode parent) throws IOException;

  protected void propagateChange() {
    if(parent != null) {
      parent.id = null;
      parent.propagateChange();
    }
  }

  protected void disconnectParent() {
    parent = null;
  }

  @Nullable
  protected abstract Snapshot takeSnapshot(boolean persist) throws IOException;

}
