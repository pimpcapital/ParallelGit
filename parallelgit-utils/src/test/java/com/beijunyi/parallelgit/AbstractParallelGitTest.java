package com.beijunyi.parallelgit;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
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
  protected AnyObjectId writeToCache(@Nonnull String path, @Nonnull byte[] content, @Nonnull FileMode mode) throws IOException {
    AnyObjectId blobId = ObjectUtils.insertBlob(content, repo);
    CacheUtils.addFile(cache, mode, path, blobId);
    return blobId;
  }

  @Nonnull
  protected AnyObjectId writeToCache(@Nonnull String path, @Nonnull byte[] content) throws IOException {
    return writeToCache(path, content, FileMode.REGULAR_FILE);
  }

  @Nonnull
  protected AnyObjectId writeToCache(@Nonnull String path, @Nonnull String content) throws IOException {
    return writeToCache(path, Constants.encode(content));
  }

  @Nonnull
  protected AnyObjectId writeToCache(@Nonnull String path) throws IOException {
    return writeToCache(path, path + "'s unique content");
  }

  @Nonnull
  protected AnyObjectId writeSomeFileToCache() throws IOException {
    return writeToCache(UUID.randomUUID().toString() + ".txt");
  }

  protected void writeFilesToCache(@Nonnull String... paths) throws IOException {
    for(String path : paths)
      writeToCache(path);
  }

  @Nonnull
  private static String generateCommitMessage() {
    return "test commit: " + System.currentTimeMillis();
  }

  @Nonnull
  protected AnyObjectId commit(@Nonnull String message, @Nullable AnyObjectId parent) throws IOException {
    return CommitUtils.createCommit(repo, cache, new PersonIdent(getClass().getSimpleName(), ""), message, parent);
  }

  @Nonnull
  protected AnyObjectId commit(@Nullable AnyObjectId parent) throws IOException {
    return commit(generateCommitMessage(), parent);
  }

  protected void updateBranchHead(@Nonnull String branch, @Nonnull AnyObjectId commit, boolean init) throws IOException {
    if(init)
      BranchUtils.initBranch(branch, commit, repo);
    else
      BranchUtils.newCommit(branch, commit, repo);
  }

  @Nonnull
  protected AnyObjectId commitToBranch(@Nonnull String branch, @Nonnull String message, @Nullable AnyObjectId parent) throws IOException {
    if(parent == null && BranchUtils.branchExists(branch, repo))
      parent = BranchUtils.getBranchHeadCommit(branch, repo);
    AnyObjectId commitId = commit(message, parent);
    updateBranchHead(branch, commitId, parent == null);
    return commitId;
  }

  @Nonnull
  protected AnyObjectId commitToBranch(@Nonnull String branch, @Nullable AnyObjectId parent) throws IOException {
    return commitToBranch(branch, generateCommitMessage(), parent);
  }

  @Nonnull
  protected AnyObjectId commitToBranch(@Nonnull String branch) throws IOException {
    return commitToBranch(branch, generateCommitMessage(), null);
  }

  @Nonnull
  protected AnyObjectId commitToMaster(@Nonnull String message, @Nullable AnyObjectId parent) throws IOException {
    return commitToBranch(Constants.MASTER, message, parent);
  }

  @Nonnull
  protected AnyObjectId commitToMaster(@Nonnull String message) throws IOException {
    return commitToBranch(Constants.MASTER, message, null);
  }

  @Nonnull
  protected AnyObjectId commitToMaster() throws IOException {
    return commitToBranch(Constants.MASTER);
  }

  protected void clearCache() {
    cache = DirCache.newInCore();
  }

  protected void initRepositoryDir() throws IOException {
    if(repoDir == null)
      repoDir = FileUtils.createTempDir(getClass().getSimpleName(), null, null);
  }

  @Nonnull
  protected AnyObjectId initContent() throws IOException {
    writeToCache("existing_file.txt");
    commitToMaster();
    writeToCache("some_other_file.txt");
    AnyObjectId head = commitToMaster();
    clearCache();
    return head;
  }

  @Nonnull
  protected AnyObjectId initRepository(boolean memory, boolean bare) throws IOException {
    if(!memory)
      initRepositoryDir();
    repo = memory ? new TestRepository(bare) : RepositoryUtils.createRepository(repoDir, bare);
    cache = DirCache.newInCore();
    return initContent();
  }

  @Nonnull
  protected AnyObjectId initFileRepository(boolean bare) throws IOException {
    return initRepository(false, bare);
  }

  @Nonnull
  protected AnyObjectId initMemoryRepository(boolean bare) throws IOException {
    return initRepository(true, bare);
  }

  @Nonnull
  protected AnyObjectId initRepository() throws IOException {
    return initRepository(true, true);
  }

  protected class TestRepository extends InMemoryRepository {

    private final File directory;
    private final File workTree;

    public TestRepository(boolean bare) {
      super(new DfsRepositoryDescription(null));
      File mockLocation = new File(System.getProperty("java.io.tmpdir"));
      directory = bare ? mockLocation : new File(mockLocation, Constants.DOT_GIT);
      workTree = bare ? null : mockLocation;
    }

    public TestRepository() {
      this(true);
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
