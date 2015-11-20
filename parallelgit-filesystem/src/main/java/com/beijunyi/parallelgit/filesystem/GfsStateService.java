package com.beijunyi.parallelgit.filesystem;

import java.nio.file.ClosedFileSystemException;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.revwalk.RevCommit;

public class GfsStateService implements AutoCloseable {

  private ReentrantLock lock = new ReentrantLock();

  private RevCommit commit;
  private String branch;

  private String mergeMessage;
  private RevCommit mergeCommit;

  private boolean closed = false;

  public GfsStateService(@Nonnull RevCommit commit, @Nullable String branch) {
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
  public RevCommit getCommit() {
    checkClosed();
    return commit;
  }

  public void setCommit(@Nonnull RevCommit commit) {
    checkClosed();
    this.commit = commit;
  }

  @Nullable
  public String getBranch() {
    checkClosed();
    return branch;
  }

  public void setBranch(@Nullable String branch) {
    checkClosed();
    this.branch = branch;
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
