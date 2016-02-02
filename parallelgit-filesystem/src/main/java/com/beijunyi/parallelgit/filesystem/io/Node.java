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

public abstract class Node<Snapshot extends ObjectSnapshot, Data> {

  protected final GfsObjectService objService;

  protected volatile DirectoryNode parent;
  protected volatile GitFileEntry origin;
  protected volatile Snapshot snapshot;
  protected volatile AnyObjectId id;
  protected volatile FileMode mode;
  protected volatile Data data;

  protected Node(@Nonnull FileMode mode, @Nonnull GfsObjectService objService) {
    this.objService = objService;
    this.mode = mode;
    initialize();
  }

  protected Node(@Nonnull AnyObjectId id, @Nonnull FileMode mode, @Nonnull GfsObjectService objService) {
    this.objService = objService;
    this.id = id;
    this.mode = mode;
  }

  protected Node(@Nonnull FileMode mode, @Nonnull DirectoryNode parent) {
    this(mode, parent.getObjectService());
    this.parent = parent;
  }

  protected Node(@Nonnull AnyObjectId id, @Nonnull FileMode mode, @Nonnull DirectoryNode parent) {
    this(id, mode, parent.getObjectService());
    this.parent = parent;
  }

  protected Node(@Nonnull Data data, @Nonnull FileMode mode, @Nonnull DirectoryNode parent) {
    this(mode, parent);
    this.data = data;
  }

  @Nonnull
  public static Node fromEntry(@Nonnull GitFileEntry entry, @Nonnull DirectoryNode parent) {
    if(entry.getMode().equals(TREE))
      return DirectoryNode.fromObject(entry.getId(), parent);
    return FileNode.fromObject(entry.getId(), entry.getMode(), parent);
  }

  @Nonnull
  protected GfsObjectService getObjectService() {
    return objService;
  }

  public boolean isExecutableFile() {
    return EXECUTABLE_FILE.equals(getMode());
  }

  public boolean isRegularFile() {
    return REGULAR_FILE.equals(getMode()) || isExecutableFile();
  }

  public boolean isSymbolicLink() {
    return SYMLINK.equals(getMode());
  }

  public boolean isDirectory() {
    return TREE.equals(getMode());
  }

  @Nullable
  public AnyObjectId getObjectId(boolean persist) throws IOException {
    if(id == null || persist && !objService.hasObject(id)) {
      Snapshot snapshot = takeSnapshot(persist);
      id = snapshot != null ? snapshot.getId() : null;
    }
    return id;
  }

  public void updateOrigin(@Nonnull GitFileEntry entry) throws IOException {
    origin = entry;
  }

  @Nonnull
  public FileMode getMode() {
    return mode;
  }

  public void setMode(@Nonnull FileMode mode) {
    checkFileMode(mode);
    this.mode = mode;
    invalidateParentCache();
  }

  public boolean isNew() throws IOException {
    return origin == null;
  }

  public boolean isModified() throws IOException {
    AnyObjectId id = getObjectId(false);
    if(origin == null)
      return id != null;
    return !origin.getId().equals(id) || !origin.getMode().equals(mode);
  }

  @Nonnull
  protected Data getData() throws IOException {
    if(data != null)
      return data;
    if(id == null)
      throw new IllegalStateException();
    data = loadData(loadSnapshot(id));
    return data;
  }

  @Nonnull
  private Snapshot loadSnapshot(@Nonnull AnyObjectId id) throws IOException {
    Snapshot ret = objService.read(id, getSnapshotType());
    if(origin != null && origin.getId().equals(id))
      snapshot = ret;
    return ret;
  }

  @Nullable
  protected Snapshot takeSnapshot(boolean persist) throws IOException {
    if(data == null)
      throw new IllegalStateException();
    if(isTrivial(data))
      return null;
    Snapshot snapshot = captureData(data, persist);
    if(persist)
      objService.write(snapshot);
    return snapshot;
  }

  @Nullable
  public Snapshot getSnapshot(boolean persist) throws IOException {
    if(data != null)
      return takeSnapshot(persist);
    if(id == null)
      throw new IllegalStateException();
    return loadSnapshot(id);
  }

  protected boolean isInitialized() {
    return data != null;
  }

  protected void initialize() {
    data = getDefaultData();
  }

  public void reset() {
    if(origin == null)
      throw new IllegalStateException();
    reset(origin);
  }

  protected void reset(@Nonnull GitFileEntry entry) {
    checkFileMode(mode);
    this.id = entry.getId();
    this.mode = entry.getMode();
    this.data = null;
    invalidateParentCache();
  }

  protected void invalidateParentCache() {
    if(parent != null) {
      parent.id = null;
      parent.invalidateParentCache();
    }
  }

  protected void exile() {
    parent = null;
  }

  protected abstract Class<? extends Snapshot> getSnapshotType();

  public abstract long getSize() throws IOException;

  protected abstract void checkFileMode(@Nonnull FileMode proposed);

  @Nonnull
  protected abstract Data getDefaultData();

  @Nonnull
  protected abstract Data loadData(@Nonnull Snapshot snapshot) throws IOException;

  protected abstract boolean isTrivial(@Nonnull Data data);

  @Nonnull
  protected abstract Snapshot captureData(@Nonnull Data data, boolean persist) throws IOException;

  @Nonnull
  protected abstract Node clone(@Nonnull DirectoryNode parent) throws IOException;

}
