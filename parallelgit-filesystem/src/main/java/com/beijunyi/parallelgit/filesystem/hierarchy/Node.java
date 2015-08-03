package com.beijunyi.parallelgit.filesystem.hierarchy;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.CopyOption;
import java.nio.file.NotDirectoryException;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitCopyOption;
import com.beijunyi.parallelgit.filesystem.GitFileStore;
import com.beijunyi.parallelgit.filesystem.GitPath;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

public abstract class Node implements Comparable<Node> {

  protected final NodeType type;
  protected String name;
  protected DirectoryNode parent;
  protected AnyObjectId object;
  protected volatile boolean loaded = false;
  protected volatile boolean dirty = false;
  protected volatile boolean locked = false;
  protected volatile long size = -1;

  private GitPath path;
  private GitFileStore store;

  protected Node(@Nonnull NodeType type, @Nonnull AnyObjectId object, @Nullable GitPath path, @Nullable GitFileStore store) {
    this.type = type;
    this.object = object;
    this.path = path;
    this.store = store;
  }

  protected Node(@Nonnull NodeType type, @Nonnull AnyObjectId object) {
    this(type, object, null, null);
  }

  @Nonnull
  protected Node forObject(@Nonnull AnyObjectId object, @Nonnull FileMode mode) {
    if(mode.equals(FileMode.TREE))
      return DirectoryNode.forTreeObject(object);
    return FileNode.forBlobObject(object, NodeType.forFileMode(mode));
  }

  @Override
  public int compareTo(@Nonnull Node that) {
    return this.path().compareTo(that.path());
  }

  @Nonnull
  public NodeType getType() {
    return type;
  }

  public boolean isRegularFile() {
    return type.isRegularFile();
  }

  public boolean isExecutableFile() {
    return type == NodeType.EXECUTABLE_FILE;
  }

  public boolean isSymbolicLink() {
    return type == NodeType.SYMBOLIC_LINK;
  }

  public boolean isDirectory() {
    return type == NodeType.DIRECTORY;
  }

  @Nonnull
  public FileNode asFile() throws AccessDeniedException {
    if(this instanceof FileNode)
      return (FileNode) this;
    throw new AccessDeniedException(path().toString());
  }

  @Nonnull
  public DirectoryNode asDirectory() throws NotDirectoryException {
    if(this instanceof DirectoryNode)
      return (DirectoryNode) this;
    throw new NotDirectoryException(path().toString());
  }

  @Nonnull
  public AnyObjectId getObject() {
    return object;
  }

  protected abstract void doLoad(@Nonnull GitFileStore store, boolean recursive) throws IOException;

  protected void load() throws IOException {
    doLoad(store(), false);
  }

  @Nonnull
  public abstract AnyObjectId doSave() throws IOException;

  @Nonnull
  public AnyObjectId save() throws IOException {
    object = doSave();
    dirty = false;
    return object;
  }

  protected void denyAccess() throws AccessDeniedException {
    throw new AccessDeniedException(path().toString());
  }

  protected void checkNotLocked() throws AccessDeniedException {
    if(locked)
      denyAccess();
  }

  public synchronized void lock() throws AccessDeniedException {
    checkNotLocked();
    locked = true;
  }

  public synchronized void unlock() throws IllegalStateException {
    if(!locked)
      throw new IllegalStateException(path().toString());
    locked = false;
  }

  protected abstract long calculateSize() throws IOException;

  public long getSize() throws IOException {
    if(size < 0)
      size = calculateSize();
    return size;
  }

  public void markDirty() {
    if(!dirty) {
      dirty = true;
      size = -1;
      if(parent != null)
        parent.markDirty();
    }
  }

  @Nonnull
  public GitPath path() {
    if(path == null)
      path = parent.path().resolve(name);
    return path;
  }

  @Nonnull
  public GitFileStore store() {
    if(store == null)
      store = parent.store();
    return store;
  }

  @Nonnull
  protected abstract Node clone(boolean deepClone) throws IOException;

  private boolean baseSameRepository(@Nonnull Node target) {
    return store().baseSameRepository(target.store());
  }

  private void amendCopyOptions(@Nonnull Node target, @Nonnull Set<CopyOption> options) {
    if(!baseSameRepository(target)) {
      options.add(GitCopyOption.CLONE);
      options.add(GitCopyOption.DEEP_CLONE);
    }
  }

  @Nonnull
  private DirectoryNode getParent() throws AccessDeniedException {
    if(parent == null)
      throw new AccessDeniedException(path().toString());
    return parent;
  }

  public void copyTo(@Nonnull DirectoryNode targetParent, @Nonnull String targetName, @Nonnull Set<CopyOption> options) throws IOException {
    options.add(GitCopyOption.CLONE);
    amendCopyOptions(targetParent, options);
    targetParent.addChild(targetName, this, options);
  }

  public void moveTo(@Nonnull DirectoryNode targetParent, @Nonnull String targetName, @Nonnull Set<CopyOption> options) throws IOException {
    String currentName = name;
    amendCopyOptions(targetParent, options);
    lock();
    targetParent.addChild(targetName, this, options);
    getParent().removeChild(currentName);
    unlock();
  }

  public void delete() throws IOException {
    lock();
    getParent().removeChild(name);
  }
}
