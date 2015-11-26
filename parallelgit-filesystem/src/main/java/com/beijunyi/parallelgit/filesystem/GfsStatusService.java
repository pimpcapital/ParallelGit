package com.beijunyi.parallelgit.filesystem;

import java.nio.file.ClosedFileSystemException;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.revwalk.RevCommit;

public class GfsStatusService implements AutoCloseable {

  private ReentrantLock lock = new ReentrantLock();

  private String branch;
  private RevCommit commit;

  private GfsState state = GfsState.NORMAL;
  private String mergeMessage;
  private RevCommit mergeCommit;

  private boolean closed = false;

  public GfsStatusService(@Nullable String branch, @Nullable RevCommit commit) {
    this.commit = commit;
    this.branch = branch;
  }

  public void lock() {
    checkClosed();
    lock.lock();
  }

  public void unlock() {
    checkClosed();
    lock.unlock();
  }

  @Nonnull
  public GfsStatus getStatus() {
    return new GfsStatus(branch, commit, state);
  }

  @Nullable
  public String getMergeMessage() {
    checkClosed();
    return mergeMessage;
  }

  public void setMergeMessage(@Nullable String mergeMessage) {
    checkClosed();
    this.mergeMessage = mergeMessage;
  }

  @Nullable
  public RevCommit getMergeCommit() {
    checkClosed();
    return mergeCommit;
  }

  public void setMergeCommit(@Nullable RevCommit mergeCommit) {
    checkClosed();
    this.mergeCommit = mergeCommit;
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
