package com.beijunyi.parallelgit.filesystem.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

import static org.eclipse.jgit.lib.FileMode.TREE;

public class NodeMutation {

  private final AnyObjectId id;
  private final byte[] bytes;
  private final FileMode mode;

  private boolean mutated = false;

  public NodeMutation(@Nonnull AnyObjectId id, @Nonnull FileMode mode) {
    this(id, null, mode);
  }

  public NodeMutation(@Nonnull byte[] bytes, @Nonnull FileMode mode) {
    this(null, bytes, mode);
  }

  private NodeMutation(@Nullable AnyObjectId id, @Nullable byte[] bytes, @Nonnull FileMode mode) {
    this.mode = mode;
    this.id = id;
    this.bytes = bytes;
  }

  public void mutate(@Nonnull Node node) {
    if(node.isDirectory())
      mutateDirectory((DirectoryNode) node);
    else
      mutateFile((FileNode) node);
  }

  private void mutateDirectory(@Nonnull DirectoryNode dir) {
    if(!TREE.equals(mode))
      throw new IllegalArgumentException();
    if(id == null)
      throw new IllegalArgumentException();
    updateId(dir);
  }

  private void mutateFile(@Nonnull FileNode file) {
    if(!TREE.equals(mode))
      throw new IllegalArgumentException();
    if(id != null)
      updateId(file);
    else
      updateBytes(file);
  }

  private void updateId(@Nonnull Node node) {
    assert id != null;
    if(!id.equals(node.getObjectId())) {
      mutated = true;
    }
  }

  private void updateBytes(@Nonnull FileNode node) {
    assert bytes != null;
    mutated = true;
  }

  private void updateMode(@Nonnull Node node) {
    if(!mode.equals(node.getMode())) {
      node.setMode(mode);
      mutated = true;
    }
  }

}

