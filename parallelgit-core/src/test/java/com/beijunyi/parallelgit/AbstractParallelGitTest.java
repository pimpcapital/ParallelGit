package com.beijunyi.parallelgit;

import java.io.File;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.*;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.util.FileUtils;
import org.junit.After;

public abstract class AbstractParallelGitTest {

  protected File repoDir;
  protected Repository repo;
  protected DirCache cache;

  @After
  public void closeRepository() throws IOException {
    if(repo != null)
      repo.close();
    if(repoDir != null && repoDir.exists())
      FileUtils.delete(repoDir, FileUtils.RECURSIVE);
  }

  @Nonnull
  protected ObjectId writeFile(@Nonnull String path, @Nonnull byte[] content, @Nonnull FileMode mode) throws IOException {
    ObjectId blobId = BlobHelper.insert(repo, content);
    DirCacheHelper.addFile(cache, mode, path, blobId);
    return blobId;
  }

  @Nonnull
  protected ObjectId writeFile(@Nonnull String path, @Nonnull byte[] content) throws IOException {
    return writeFile(path, content, FileMode.REGULAR_FILE);
  }

  @Nonnull
  protected ObjectId writeFile(@Nonnull String path, @Nonnull String content) throws IOException {
    return writeFile(path, Constants.encode(content));
  }

  @Nonnull
  protected ObjectId writeFile(@Nonnull String path) throws IOException {
    return writeFile(path, path + "'s unique content");
  }

  @Nonnull
  protected ObjectId writeSomeFile() throws IOException {
    return writeFile("some_file.txt");
  }

  protected void writeFiles(@Nonnull String... paths) throws IOException {
    for(String path : paths)
      writeFile(path);
  }

  @Nonnull
  protected ObjectId commit(@Nonnull String message, @Nullable ObjectId parent) throws IOException {
    return CommitHelper.createCommit(repo, cache, new PersonIdent(getClass().getSimpleName(), ""), message, parent);
  }

  protected void updateBranchHead(@Nonnull String branch, @Nonnull ObjectId commitId) throws IOException {
    BranchHelper.commitBranchHead(repo, branch, commitId, CommitHelper.getCommit(repo, commitId).getShortMessage());
  }

  @Nonnull
  protected ObjectId commitToBranch(@Nonnull String branch, @Nonnull String message, @Nullable ObjectId parent) throws IOException {
    if(parent == null)
      parent = BranchHelper.getBranchHeadCommitId(repo, branch);
    ObjectId commitId = commit(message, parent);
    updateBranchHead(branch, commitId);
    return commitId;
  }

  @Nonnull
  protected ObjectId commitToBranch(@Nonnull String branch, @Nullable ObjectId parent) throws IOException {
    return commitToBranch(branch, getClass().getSimpleName() + " test commit: " + System.currentTimeMillis(), parent);
  }

  @Nonnull
  protected ObjectId commitToBranch(@Nonnull String branch) throws IOException {
    return commitToBranch(branch, getClass().getSimpleName() + " test commit: " + System.currentTimeMillis(), null);
  }

  @Nonnull
  protected ObjectId commitToMaster(@Nonnull String message, @Nullable ObjectId parent) throws IOException {
    return commitToBranch(Constants.MASTER, message, parent);
  }

  @Nonnull
  protected ObjectId commitToMaster(@Nonnull String message) throws IOException {
    return commitToBranch(Constants.MASTER, message, null);
  }

  @Nonnull
  protected ObjectId commitToMaster() throws IOException {
    return commitToBranch(Constants.MASTER);
  }

  protected void clearCache() {
    cache = DirCache.newInCore();
  }

  protected void initRepositoryDir(boolean memory) throws IOException {
    if(repoDir == null)
      repoDir = memory ? new File("/memory").getAbsoluteFile() : FileUtils.createTempDir(getClass().getSimpleName(), null, null);
  }

  @Nonnull
  protected ObjectId initRepository(boolean memory, boolean bare) throws IOException {
    initRepositoryDir(memory);
    repo = memory ? new TestRepository(getClass().getName(), repoDir, bare) : RepositoryHelper.createRepository(repoDir, bare);
    cache = DirCache.newInCore();
    writeFile("existing_file1.txt");
    commitToMaster();
    writeFile("existing_file2.txt");
    return commitToMaster();
  }

  @Nonnull
  protected ObjectId initFileRepository(boolean bare) throws IOException {
    return initRepository(false, bare);
  }

  @Nonnull
  protected ObjectId initRepository() throws IOException {
    return initRepository(true, true);
  }

  protected class TestRepository extends InMemoryRepository {

    private final File directory;
    private final File workTree;

    public TestRepository(@Nonnull String name, @Nonnull File mockLocation, boolean bare) {
      super(new DfsRepositoryDescription(name));
      directory = bare ? mockLocation : new File(mockLocation, Constants.DOT_GIT);
      workTree = bare ? null : mockLocation;
    }

    @Override
    public boolean isBare() {
      return workTree == null;
    }

    @Nonnull
    @Override
    public File getWorkTree() throws NoWorkTreeException {
      if(workTree == null)
        throw new NoWorkTreeException();
      return workTree;
    }

    @Nonnull
    @Override
    public File getDirectory() {
      return directory;
    }

  }

}
