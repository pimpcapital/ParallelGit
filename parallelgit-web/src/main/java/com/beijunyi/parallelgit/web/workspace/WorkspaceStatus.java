package com.beijunyi.parallelgit.web.workspace;

import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.web.workspace.status.Head;

public class WorkspaceStatus {

  private final Head head;

  public WorkspaceStatus(@Nonnull Head head) {
    this.head = head;
  }

  @Nonnull
  public Head getHead() {
    return head;
  }

}
