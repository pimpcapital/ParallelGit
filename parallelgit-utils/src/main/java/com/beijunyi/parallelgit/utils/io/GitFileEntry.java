package com.beijunyi.parallelgit.utils.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.treewalk.TreeWalk;

import static org.eclipse.jgit.lib.FileMode.*;
import static org.eclipse.jgit.lib.ObjectId.zeroId;

public class GitFileEntry {

  public static final GitFileEntry TRIVIAL = new GitFileEntry(ObjectId.zeroId(), FileMode.MISSING);

  private final ObjectId id;
  private final FileMode mode;

  public GitFileEntry(ObjectId id, FileMode mode) {
    this.id = id;
    this.mode = mode;
  }

  @Nonnull
  public static GitFileEntry tree(ObjectId id) {
    return new GitFileEntry(id, TREE);
  }

  @Nonnull
  public static GitFileEntry forTreeNode(TreeWalk tw, int index) {
    return new GitFileEntry(tw.getObjectId(index), tw.getFileMode(index));
  }

  @Nonnull
  public ObjectId getId() {
    return id;
  }

  @Nonnull
  public FileMode getMode() {
    return mode;
  }

  public boolean isRegularFile() {
    return mode.equals(REGULAR_FILE);
  }

  public boolean isExecutableFile() {
    return mode.equals(EXECUTABLE_FILE);
  }

  public boolean isDirectory() {
    return mode.equals(TREE);
  }

  public boolean isVirtualDirectory() {
    return isDirectory() && zeroId().equals(id);
  }

  public boolean isSymbolicLink() {
    return mode.equals(SYMLINK);
  }

  public boolean isGitLink() {
    return mode.equals(GITLINK);
  }

  public boolean isMissing() {
    return mode.equals(MISSING);
  }

  public boolean hasSameObjectAs(GitFileEntry entry) {
    return id.equals(entry.getId());
  }

  public boolean hasSameModeAs(GitFileEntry entry) {
    return mode.equals(entry.getMode());
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if(this == obj) return true;
    if(obj == null || getClass() != obj.getClass()) return false;
    GitFileEntry that = (GitFileEntry)obj;
    return id.equals(that.id) && mode.equals(that.mode);

  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + mode.hashCode();
    return result;
  }
}
