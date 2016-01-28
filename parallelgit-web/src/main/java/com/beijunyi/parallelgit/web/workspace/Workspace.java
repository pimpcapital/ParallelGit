package com.beijunyi.parallelgit.web.workspace;

import javax.annotation.Nonnull;

public class Workspace {

  private final String id;
  private final GitUser user;

  public Workspace(@Nonnull String id, @Nonnull GitUser user) {
    this.id = id;
    this.user = user;
  }


}
