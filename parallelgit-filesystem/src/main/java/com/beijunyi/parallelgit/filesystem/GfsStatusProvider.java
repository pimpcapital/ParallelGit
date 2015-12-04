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
  public GfsStatusUpdater prepareUpdate(@Nonnull GfsState state) {
    checkClosed();
    lock.lock();
    return new GfsStatusUpdater();
  }

  public void completeUpdate(@Nonnull GfsState state, @Nonnull GfsStatusUpdater updater) {
    checkClosed();

    lock.unlock();
  }

  @Nonnull
  public GfsStatus get() {
    checkClosed();
    return new GfsStatus(state, branch, commit, mergeNote);
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
