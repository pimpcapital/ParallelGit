package com.beijunyi.parallelgit.web.protocol.model;

import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;

public class Head {

  private final String name;
  private final HeadType type;

  private Head(@Nonnull String name, @Nonnull HeadType type) {
    this.name = name;
    this.type = type;
  }

  @Nonnull
  public static Head commit(@Nonnull String id) {
    return new Head(id, HeadType.COMMIT);
  }

  @Nonnull
  public static Head branch(@Nonnull String name) {
    return new Head(name, HeadType.BRANCH);
  }

  @Nonnull
  public String getName() {
    return name;
  }

  @Nonnull
  public HeadType getType() {
    return type;
  }

}
