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

  protected final GfsObjectService gds;

  protected volatile AnyObjectId id;

  protected Node(@Nullable AnyObjectId id, @Nonnull GfsObjectService gds) {
    this.id = id;
    this.gds = gds;
  }

  @Nonnull
  public static Node fromEntry(@Nonnull GitFileEntry entry, @Nonnull GfsObjectService gds) {
    if(entry.getMode() == TREE)
      return DirectoryNode.fromObject(entry.getId(), gds);
    return FileNode.fromObject(entry.getId(), entry.getMode(), gds);
  }

  @Nonnull
  public GfsObjectService getDataService() {
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

  public abstract long getSize() throws IOException;

  @Nonnull
  public abstract FileMode getMode();

  public abstract void setMode(@Nonnull FileMode mode);

  public abstract void reset(@Nonnull AnyObjectId id);

  @Nullable
  public abstract Snapshot loadSnapshot() throws IOException;

  @Nullable
  public abstract Snapshot takeSnapshot(boolean persist, boolean allowEmpty) throws IOException;

  @Nonnull
  public abstract Node clone(@Nonnull GfsObjectService targetGds) throws IOException;

}
