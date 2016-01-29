package com.beijunyi.parallelgit.web.data;

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
  public static Head of(@Nonnull GitFileSystem gfs) {
    GfsStatusProvider status = gfs.getStatusProvider();
    if(status.isAttached())
      return new Head(status.branch(), HeadType.BRANCH);
    else
      return new Head(status.commit().getName(), HeadType.COMMIT);
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
