package com.beijunyi.parallelgit.web.protocol.model;

import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;

public class Status {

  private final Head head;

  private Status(@Nonnull Head head) {
    this.head = head;
  }

  @Nonnull
  public static Status of(@Nonnull GitFileSystem gfs) {
    GfsStatusProvider status = gfs.getStatusProvider();
    Head head = status.isAttached() ? Head.branch(status.branch()) : Head.commit(status.commit().getName());
    return new Status(head);
  }

  @Nonnull
  public Head getHead() {
    return head;
  }

}
