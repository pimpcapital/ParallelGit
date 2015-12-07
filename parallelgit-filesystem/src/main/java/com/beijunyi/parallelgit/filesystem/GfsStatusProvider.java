package com.beijunyi.parallelgit.filesystem;

import java.nio.file.ClosedFileSystemException;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.exceptions.MergeNotStartedException;
import com.beijunyi.parallelgit.filesystem.exceptions.NoBranchException;
import com.beijunyi.parallelgit.filesystem.exceptions.NoHeadCommitException;
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

  private void lock() {
    lock.lock();
  }

  private void unlock() {
    lock.unlock();
  }

  @Nonnull
  public GfsState state() {
    checkClosed();
    return state;
  }

  @Nonnull
  public GfsStatusProvider state(@Nonnull GfsState state) {
    checkClosed();
    this.state = state;
    return this;
  }

  @Nonnull
  public String branch() {
    checkClosed();
    if(branch == null)
      throw new NoBranchException();
    return branch;
  }

  @Nonnull
  public GfsStatusProvider branch(@Nonnull String branch) {
    checkClosed();
    this.branch = branch;
    return this;
  }

  public boolean isAttached() {
    return branch != null;
  }

  @Nonnull
  public RevCommit commit() {
    checkClosed();
    if(commit == null)
      throw new NoHeadCommitException();
    return commit;
  }

  @Nonnull
  public GfsStatusProvider commit(@Nonnull RevCommit commit) {
    checkClosed();
    this.commit = commit;
    return this;
  }

  public boolean isInitialized() {
    return branch != null;
  }

  @Nonnull
  public GfsMergeNote mergeNote() {
    checkClosed();
    if(mergeNote == null)
      throw new MergeNotStartedException();
    return mergeNote;
  }


  @Nonnull
  public GfsStatusProvider mergeNote(@Nonnull GfsMergeNote mergeNote) {
    checkClosed();
    this.mergeNote = mergeNote;
    return this;
  }

  @Nonnull
  public GfsStatusProvider clearMergeNote() {
    mergeNote = null;
    return this;
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
