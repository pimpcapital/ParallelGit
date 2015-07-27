package com.beijunyi.parallelgit.filesystem.hierarchy;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.CopyOption;
import java.nio.file.NotDirectoryException;
import java.util.Set;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitPath;
import com.beijunyi.parallelgit.filesystem.utils.GitCopyOption;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Repository;

public abstract class Node implements Comparable<Node> {

  protected final NodeType type;
  protected Repository repository;
  protected String name;
  protected GitPath path;
  protected DirectoryNode parent;
  protected AnyObjectId object;
  protected volatile boolean loaded = false;
  protected volatile boolean dirty = false;
  protected volatile boolean locked = false;
  protected volatile long size = -1;

  protected Node(@Nonnull NodeType type, @Nonnull AnyObjectId object) {
    this.type = type;
    this.object = object;
  }

  @Override
  public int compareTo(@Nonnull Node that) {
    return this.getPath().compareTo(that.getPath());
  }

  public boolean isRegularFile() {
    return type == NodeType.NON_EXECUTABLE_FILE || type == NodeType.EXECUTABLE_FILE;
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
    throw new AccessDeniedException(getPath().toString());
  }

  @Nonnull
  public DirectoryNode asDirectory() throws NotDirectoryException {
    if(this instanceof DirectoryNode)
      return (DirectoryNode) this;
    throw new NotDirectoryException(getPath().toString());
  }

  @Nonnull
  public Repository getRepository() {
    if(repository == null)
      repository = parent.getRepository();
    return repository;
  }

  @Nonnull
  public GitPath getPath() {
    if(path == null)
      path = parent.getPath().resolve(name);
    return path;
  }

  @Nonnull
  public AnyObjectId getObject() {
    return object;
  }

  protected abstract void doLoad(boolean recursive) throws IOException;

  protected synchronized void load(boolean recursive) throws IOException {
    if(!loaded || recursive) {
      doLoad(recursive);
      loaded = true;
    }
  }

  protected void checkLoaded() {
    if(!loaded)
      throw new IllegalStateException();
  }

  protected abstract boolean doUnload(boolean recursive);

  protected synchronized void unload(boolean recursive) {
    if((loaded || recursive) && doUnload(recursive))
      loaded = false;
  }

  public boolean isDirty() {
    return dirty;
  }

  protected void denyAccess() throws AccessDeniedException {
    throw new AccessDeniedException(getPath().toString());
  }

  protected void checkLocked() throws AccessDeniedException {
    if(locked)
      denyAccess();
  }

  public synchronized void lock() throws AccessDeniedException {
    checkLocked();
    locked = true;
  }

  public synchronized void unlock() throws IllegalStateException {
    if(!locked)
      throw new IllegalStateException(getPath().toString());
    locked = false;
  }

  protected abstract long calculateSize() throws IOException;

  public void invalidateSize() {
    if(size >= 0) {
      size = -1;
      if(parent != null)
        parent.invalidateSize();
    }
  }

  public long getSize() throws IOException {
    if(size < 0)
      size = calculateSize();
    return size;
  }

  public abstract void markDirty(boolean recursive);

  public void markAncestorDirty() {
    if(!dirty) {
      markDirty(false);
      if(parent != null)
        parent.markAncestorDirty();
    }
  }

  @Nonnull
  protected abstract Node prepareClone() throws AccessDeniedException;

  @Nonnull
  public Node makeClone() throws AccessDeniedException{
    Node clone = prepareClone();
    clone.repository = repository;
    clone.loaded = loaded;
    clone.dirty = dirty;
    clone.size = size;
    return clone;
  }

  private boolean baseSameRepository(@Nonnull Node target) {
    return getRepository().getDirectory().equals(target.getRepository().getDirectory());
  }

  private void amendCopyOptions(@Nonnull Node target, @Nonnull Set<CopyOption> options) {
    if(!baseSameRepository(target)) {
      options.add(GitCopyOption.LOAD_BEFORE_COPY);
      if(options.contains(GitCopyOption.CLONE))
        options.add(GitCopyOption.MARK_DIRTY);
    }
  }

  @Nonnull
  private DirectoryNode getParent() throws AccessDeniedException {
    if(parent == null)
      throw new AccessDeniedException(getPath().toString());
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
