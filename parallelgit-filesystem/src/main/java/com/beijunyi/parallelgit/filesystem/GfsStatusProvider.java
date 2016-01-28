package com.beijunyi.parallelgit.filesystem;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.ClosedFileSystemException;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.exceptions.MergeNotStartedException;
import com.beijunyi.parallelgit.filesystem.exceptions.NoBranchException;
import com.beijunyi.parallelgit.filesystem.exceptions.NoHeadCommitException;
import com.beijunyi.parallelgit.filesystem.merge.GfsMergeNote;
import com.beijunyi.parallelgit.utils.RefUtils;
import com.beijunyi.parallelgit.utils.io.TreeSnapshot;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import static org.eclipse.jgit.lib.Constants.R_HEADS;

public class GfsStatusProvider implements AutoCloseable {

  private final ReentrantLock lock = new ReentrantLock();

  private final GfsFileStore fileStore;

  private GfsState state = GfsState.NORMAL;
  private String branch;
  private RevCommit commit;
  private GfsMergeNote mergeNote;

  private boolean closed = false;

  public GfsStatusProvider(@Nonnull GfsFileStore fileStore, @Nullable String branch, @Nullable RevCommit commit) {
    this.fileStore = fileStore;
    this.commit = commit;
    this.branch = branch;
  }

  public boolean isDirty() throws IOException {
    return fileStore.getRoot().isDirty();
  }

  @Nonnull
  public Update prepareUpdate() {
    return new Update();
  }

  @Nonnull
  public GfsState state() {
    checkClosed();
    return state;
  }

  @Nonnull
  public String branch() {
    checkClosed();
    if(branch == null)
      throw new NoBranchException();
    return branch.substring(R_HEADS.length());
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

  public boolean isInitialized() {
    return branch != null;
  }

  @Nonnull
  public GfsMergeNote mergeNote() {
    checkClosed();
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

  public class Update implements Closeable {

    private Update() {
      lock.lock();
    }

    @Override
    public void close() {
      lock.unlock();
    }

    @Nonnull
    public Update state(@Nonnull GfsState newState) {
      checkClosed();
      state = newState;
      return this;
    }

    @Nonnull
    public Update branch(@Nonnull String newBranch) {
      checkClosed();
      branch = RefUtils.ensureBranchRefName(newBranch);
      return this;
    }

    @Nonnull
    public Update detach() {
      checkClosed();
      branch = null;
      return this;
    }

    @Nonnull
    public Update commit(@Nonnull RevCommit newCommit) {
      checkClosed();
      commit = newCommit;
      return this;
    }

    @Nonnull
    public Update mergeNote(@Nonnull GfsMergeNote newMergeNote) {
      checkClosed();
      mergeNote = newMergeNote;
      return this;
    }

    @Nonnull
    public Update clearMergeNote() {
      checkClosed();
      mergeNote = null;
      return this;
    }

  }

}
