package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsObjectService;
import com.beijunyi.parallelgit.utils.io.GitFileEntry;
import com.beijunyi.parallelgit.utils.io.ObjectSnapshot;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import static com.beijunyi.parallelgit.filesystem.io.DirectoryNode.fromTree;
import static com.beijunyi.parallelgit.filesystem.io.FileNode.fromBlob;
import static com.beijunyi.parallelgit.utils.io.GitFileEntry.missingEntry;
import static org.eclipse.jgit.lib.FileMode.*;
import static org.eclipse.jgit.lib.ObjectId.zeroId;

public abstract class Node<Snapshot extends ObjectSnapshot, Data> {

  protected final GfsObjectService objService;

  protected volatile GitFileEntry origin = missingEntry();
  protected volatile DirectoryNode parent;
  protected volatile Snapshot snapshot;
  protected volatile ObjectId id;
  protected volatile FileMode mode;
  protected volatile Data data;

  protected Node(FileMode mode, GfsObjectService objService) {
    this.objService = objService;
    this.mode = mode;
    initialize();
  }

  protected Node(ObjectId id, FileMode mode, GfsObjectService objService) {
    this.objService = objService;
    this.id = id;
    this.mode = mode;
  }

  protected Node(FileMode mode, DirectoryNode parent) {
    this(mode, parent.getObjectService());
    this.parent = parent;
  }

  protected Node(ObjectId id, FileMode mode, DirectoryNode parent) {
    this(id, mode, parent.getObjectService());
    this.parent = parent;
  }

  protected Node(Data data, FileMode mode, DirectoryNode parent) {
    this(mode, parent);
    this.data = data;
  }

  @Nonnull
  public static Node fromEntry(GitFileEntry entry, DirectoryNode parent) {
    return TREE.equals(entry.getMode())
             ? fromTree(entry.getId(), parent)
             : fromBlob(entry.getId(), entry.getMode(), parent);
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

  @Nonnull
  public ObjectId getObjectId(boolean persist) throws IOException {
    if(id == null || persist && !objService.hasObject(id)) {
      Snapshot snapshot = takeSnapshot(persist);
      id = snapshot != null ? snapshot.getId() : zeroId();
    }
    return id;
  }

  @Nonnull
  public GitFileEntry getOrigin() {
    return origin;
  }

  public void updateOrigin(GitFileEntry entry) throws IOException {
    origin = entry;
  }

  @Nonnull
  public FileMode getMode() {
    return mode;
  }

  public void setMode(FileMode mode) {
    checkFileMode(mode);
    this.mode = mode;
    invalidateParentCache();
  }

  public boolean isNew() throws IOException {
    return origin.isMissing();
  }

  public boolean isModified() throws IOException {
    ObjectId id = getObjectId(false);
    return !origin.getId().equals(id) || !origin.getMode().equals(mode);
  }

  @Nonnull
  protected Data getData() throws IOException {
    if(data != null)
      return data;
    if(id == null) throw new IllegalStateException();
    data = loadData(loadSnapshot(id));
    return data;
  }

  protected boolean isTrivial() throws IOException {
    return isTrivial(getObjectId(false));
  }

  @Nonnull
  protected Snapshot loadSnapshot(ObjectId id) throws IOException {
    Snapshot ret = objService.read(id, getSnapshotType());
    if(origin.getId().equals(id))
      snapshot = ret;
    return ret;
  }

  @Nullable
  protected Snapshot takeSnapshot(boolean persist) throws IOException {
    if(data == null) throw new IllegalStateException();
    if(isTrivial(data)) return null;
    Snapshot snapshot = captureData(data, persist);
    if(persist) objService.write(snapshot);
    return snapshot;
  }

  protected boolean isInitialized() {
    return data != null;
  }

  protected void initialize() {
    data = getDefaultData();
  }

  public void reset() {
    if(origin.isMissing()) throw new IllegalStateException();
    reset(origin);
  }

  protected void reset(GitFileEntry entry) {
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

  protected abstract void checkFileMode(FileMode proposed);

  @Nonnull
  protected abstract Data getDefaultData();

  @Nonnull
  protected abstract Data loadData(Snapshot snapshot) throws IOException;

  protected abstract boolean isTrivial(Data data) throws IOException;

  @Nonnull
  protected abstract Snapshot captureData(Data data, boolean persist) throws IOException;

  @Nonnull
  protected abstract Node clone(DirectoryNode parent) throws IOException;

  protected static boolean isTrivial(ObjectId id) {
    return zeroId().equals(id);
  }

}
