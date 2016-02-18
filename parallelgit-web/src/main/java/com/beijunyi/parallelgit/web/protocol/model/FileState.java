package com.beijunyi.parallelgit.web.protocol.model;

import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.web.data.FileType;

public class FileState {

  private final String hash;
  private final FileType type;

  public FileState(@Nonnull String hash, @Nonnull FileType type) {
    this.hash = hash;
    this.type = type;
  }

  @Nonnull
  public String getHash() {
    return hash;
  }

  @Nonnull
  public FileType getType() {
    return type;
  }

}
