package com.beijunyi.parallelgit.filesystem.hierarchy;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitPath;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectReader;

public abstract class Node implements Comparable<Node> {

  protected final NodeType type;
  protected ObjectReader reader;
  protected String name;
  protected GitPath path;
  protected DirectoryNode parent;
  protected AnyObjectId object;
  protected boolean loaded = false;
  protected boolean dirty = false;
  protected boolean locked = false;
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
  public FileNode asFile() {
    return (FileNode) this;
  }

  @Nonnull
  public DirectoryNode asDirectory() {
    return (DirectoryNode) this;
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

  protected abstract void doLoad() throws IOException;

  protected synchronized void load() throws IOException {
    if(!loaded) {
      loaded = true;
      doLoad();
    }
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

  public void markDirty() {
    if(!dirty) {
      dirty = true;
      if(parent != null)
        parent.markDirty();
    }
  }

  @Nonnull
  public DirectoryNode ensureParent() throws IOException {
    if(parent == null)
      denyAccess();
     return parent;
  }

  public void delete() throws IOException {
    ensureParent().deleteChild(name);
  }

  @Nonnull
  protected abstract Node prepareClone();

  @Nonnull
  public Node makeClone() {
    Node clone = prepareClone();
    clone.reader = reader;
    clone.loaded = loaded;
    clone.dirty = dirty;
    clone.size = size;
    return clone;
  }

}
