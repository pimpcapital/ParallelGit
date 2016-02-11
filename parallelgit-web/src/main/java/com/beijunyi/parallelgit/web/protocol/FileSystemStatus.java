package com.beijunyi.parallelgit.web.protocol;

import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;

public class FileSystemStatus {

  private final String head;
  private final boolean attached;

  private FileSystemStatus(@Nonnull String head, boolean attached) {
    this.head = head;
    this.attached = attached;
  }

  @Nonnull
  public static FileSystemStatus readStatus(@Nonnull GitFileSystem gfs) {
    GfsStatusProvider status = gfs.getStatusProvider();
    if(status.isAttached())
      return new FileSystemStatus(status.branch(), true);
    return new FileSystemStatus(status.commit().getName(), false);
  }

  @Nonnull
  public String getHead() {
    return head;
  }

  public boolean isAttached() {
    return attached;
  }

}
