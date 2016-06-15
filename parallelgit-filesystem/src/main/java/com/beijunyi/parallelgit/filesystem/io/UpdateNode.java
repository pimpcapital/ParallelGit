package com.beijunyi.parallelgit.filesystem.io;

import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.io.GitFileEntry;

import static com.beijunyi.parallelgit.filesystem.io.Node.fromEntry;

public class UpdateNode extends GfsChange {

  private final GitFileEntry entry;

  public UpdateNode(GitFileEntry entry) {
    this.entry = entry;
  }

  @Nullable
  @Override
  protected Node convertNode(@Nullable Node node, DirectoryNode parent) {
    return fromEntry(entry, parent);
  }

}
