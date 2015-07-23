package com.beijunyi.parallelgit.filesystem.hierarchy;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectReader;

public abstract class TreeNode {

  protected final TreeNodeType type;
  protected String name;
  protected AnyObjectId object;
  protected TreeNode parent;
  protected boolean loaded = false;
  protected boolean dirty = false;
  protected boolean locked = false;

  protected TreeNode(@Nonnull TreeNodeType type) {
    this.type = type;
  }

  protected abstract void doLoad(@Nonnull ObjectReader reader) throws IOException;

  synchronized public void load(@Nonnull ObjectReader reader) throws IOException {
    if(!loaded)
      doLoad(reader);
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

  protected void denyAccess() throws AccessDeniedException {
    throw new AccessDeniedException(getPath());
  }

  synchronized public void lock() throws AccessDeniedException {
    if(locked)
      denyAccess();
    locked = true;
  }

  synchronized public void unlock() throws IllegalStateException {
    if(!locked)
      throw new IllegalStateException(getPath());
    locked = false;
  }

}
