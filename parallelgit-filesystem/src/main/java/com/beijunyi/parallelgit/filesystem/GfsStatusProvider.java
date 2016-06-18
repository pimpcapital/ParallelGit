package com.beijunyi.parallelgit.filesystem;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.ClosedFileSystemException;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.exceptions.NoBranchException;
import com.beijunyi.parallelgit.filesystem.exceptions.NoHeadCommitException;
import com.beijunyi.parallelgit.filesystem.merge.MergeNote;
import com.beijunyi.parallelgit.utils.RefUtils;
import org.eclipse.jgit.revwalk.RevCommit;

import static org.eclipse.jgit.lib.Constants.R_HEADS;

public class GfsStatusProvider implements AutoCloseable {

  private final ReentrantLock lock = new ReentrantLock();

  private final GfsFileStore fileStore;

  private String branch;
  private RevCommit commit;
  private MergeNote mergeNote;

  private volatile boolean closed = false;

  public GfsStatusProvider(GfsFileStore fileStore, @Nullable String branch, @Nullable RevCommit commit) {
    this.fileStore = fileStore;
    this.commit = commit;
    this.branch = branch;
  }

  public boolean isDirty() throws IOException {
    return fileStore.getRoot().isModified();
  }

  @Nonnull
  public Update prepareUpdate() {
    return new Update();
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

  @Nullable
  public MergeNote mergeNote() {
    checkClosed();
    return mergeNote;
  }

  @Override
  public synchronized void close() {
    if(!closed) closed = true;
  }

  private void checkClosed() {
    if(closed) throw new ClosedFileSystemException();
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
    public Update branch(String newBranch) {
      checkClosed();
      branch = RefUtils.fullBranchName(newBranch);
      return this;
    }

    @Nonnull
    public Update detach() {
      checkClosed();
      branch = null;
      return this;
    }

    @Nonnull
    public Update commit(RevCommit newCommit) {
      checkClosed();
      commit = newCommit;
      return this;
    }

    @Nonnull
    public Update mergeNote(MergeNote newMergeNote) {
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
