package com.beijunyi.parallelgit.filesystem;

import java.nio.file.ClosedFileSystemException;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.merge.GfsMergeNote;
import org.eclipse.jgit.revwalk.RevCommit;

public class GfsStatusProvider implements AutoCloseable {

  private final ReentrantLock lock = new ReentrantLock();

  private GfsState state = GfsState.NORMAL;
  private String branch;
  private RevCommit commit;
  private GfsMergeNote mergeNote;

  private boolean closed = false;

  public GfsStatusProvider(@Nullable String branch, @Nullable RevCommit commit) {
    this.commit = commit;
    this.branch = branch;
  }

  @Nonnull
  public GfsStatusUpdate prepareUpdate(@Nonnull GfsState state) {
    checkClosed();
    lock.lock();
    return new GfsStatusUpdate(this);
  }

  public void completeUpdate(@Nonnull GfsStatusUpdate update) {
    checkClosed();

    lock.unlock();
  }


  @Nullable
  public String branch() {
    return branch;
  }

  @Nullable
  public RevCommit commit() {
    return commit;
  }

  @Nonnull
  public GfsState state() {
    return state;
  }

  @Nullable
  public GfsMergeNote mergeNote() {
    return mergeNote;
  }

  @Override
  public void close() {
    if(!closed)
      closed = true;
  }

  private void checkClosed() {
    if(closed)
      throw new ClosedFileSystemException();
  }
}
