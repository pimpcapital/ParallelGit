package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
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
    if(entry.getMode() == TREE)
      return DirectoryNode.fromObject(entry.getId(), gds);
    return FileNode.fromObject(entry.getId(), entry.getMode(), gds);
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
  public AnyObjectId getObjectId() {
    return id;
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

  @Nullable
  public AnyObjectId persist() throws IOException {
    snapshot = takeSnapshot();
    if(snapshot != null)
      id = gds.write(snapshot);
    return id;
  }

  @Nonnull
  public abstract FileMode getMode();

  @Nullable
  public abstract Snapshot loadSnapshot() throws IOException;

  @Nullable
  public abstract Snapshot takeSnapshot() throws IOException;

}
