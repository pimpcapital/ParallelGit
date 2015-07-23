package com.beijunyi.parallelgit.filesystem.hierarchy;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectReader;

public abstract class TreeNode {

  protected final TreeNodeType type;
  protected final ObjectReader reader;
  protected String name;
  protected AnyObjectId object;
  protected TreeNode parent;
  protected boolean loaded = false;
  protected boolean dirty = false;
  protected boolean locked = false;

  protected TreeNode(@Nonnull TreeNodeType type, @Nonnull ObjectReader reader) {
    this.type = type;
    this.reader = reader;
  }

  public boolean isRegularFile() {
    return type == TreeNodeType.NON_EXECUTABLE_FILE || type == TreeNodeType.EXECUTABLE_FILE;
  }

  public boolean isExecutableFile() {
    return type == TreeNodeType.EXECUTABLE_FILE;
  }

  public boolean isSymbolicLink() {
    return type == TreeNodeType.SYMBOLIC_LINK;
  }

  public boolean isDirectory() {
    return type == TreeNodeType.DIRECTORY;
  }

  @Nonnull
  public AnyObjectId getObject() {
    return object;
  }

  protected abstract void doLoad() throws IOException;

  protected synchronized void load() throws IOException {
    if(!loaded)
      doLoad();
    loaded = true;
  }

  public boolean isAncestor(@Nonnull TreeNode node) {
    return node == parent || parent.isAncestor(node);
  }

  @Nonnull
  public String getPath() {
    if(parent == null)
      return "";
    String base = parent.getPath();
    if(base.isEmpty())
      return name;
    return base + "/" + name;
  }

  protected void failLock() throws AccessDeniedException {
    throw new AccessDeniedException(getPath());
  }

  public synchronized void lock() throws AccessDeniedException {
    if(locked)
      failLock();
    locked = true;
  }

  protected void failUnlock() throws IllegalStateException {
    throw new IllegalStateException(getPath());
  }

  public synchronized void unlock() throws IllegalStateException {
    if(!locked)
      failUnlock();
    locked = false;
  }

}
