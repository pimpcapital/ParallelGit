package com.beijunyi.parallelgit.filesystem.io;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.FileMode;

import static com.beijunyi.parallelgit.filesystem.io.FileNode.fromBytes;

public class UpdateFile extends GfsChange {

  private final byte[] bytes;
  private final FileMode mode;

  public UpdateFile(byte[] bytes, FileMode mode) {
    this.bytes = bytes;
    this.mode = mode;
  }

  @Nullable
  @Override
  protected Node convertNode(@Nullable Node node, DirectoryNode parent) {
    return fromBytes(bytes, mode, parent);
  }

}
