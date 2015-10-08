package com.beijunyi.parallelgit;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.*;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.FileUtils;
import org.junit.After;
import org.junit.Assert;

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
    CacheUtils.addFile(path, mode, blobId, cache);
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
  protected AnyObjectId someObjectId() {
    return ObjectUtils.calculateBlobId(UUID.randomUUID().toString().getBytes());
  }

  @Nonnull
  protected String someCommitMessage() {
    return getClass().getSimpleName() + " commit: " + UUID.randomUUID().toString();
  }

  @Nonnull
  protected PersonIdent somePersonIdent() {
    String name = getClass().getSimpleName();
    return new PersonIdent(name, name + "@test.com");
  }

  @Nonnull
  protected RevCommit commit(@Nonnull String message, @Nullable AnyObjectId parent) throws IOException {
    return CommitUtils.createCommit(message, cache, somePersonIdent(), parent, repo);
  }

  @Nonnull
  protected RevCommit commit(@Nullable AnyObjectId parent) throws IOException {
    return commit(someCommitMessage(), parent);
  }

  protected void updateBranchHead(@Nonnull String branch, @Nonnull AnyObjectId commit, boolean init) throws IOException {
    if(init)
      BranchUtils.initBranch(branch, commit, repo);
    else
      BranchUtils.newCommit(branch, commit, repo);
  }

  @Nonnull
  protected RevCommit commitToBranch(@Nonnull String branch, @Nonnull String message, @Nullable AnyObjectId parent) throws IOException {
    if(parent == null && BranchUtils.branchExists(branch, repo))
      parent = BranchUtils.getBranchHeadCommit(branch, repo);
    RevCommit commitId = commit(message, parent);
    updateBranchHead(branch, commitId, parent == null);
    return commitId;
  }

  @Nonnull
  protected RevCommit commitToBranch(@Nonnull String branch, @Nullable AnyObjectId parent) throws IOException {
    return commitToBranch(branch, someCommitMessage(), parent);
  }

  @Nonnull
  protected RevCommit commitToBranch(@Nonnull String branch) throws IOException {
    return commitToBranch(branch, someCommitMessage(), null);
  }

  @Nonnull
  protected RevCommit commitToMaster(@Nonnull String message, @Nullable AnyObjectId parent) throws IOException {
    return commitToBranch(Constants.MASTER, message, parent);
  }

  @Nonnull
  protected RevCommit commitToMaster(@Nonnull String message) throws IOException {
    return commitToBranch(Constants.MASTER, message, null);
  }

  @Nonnull
  protected RevCommit commitToMaster() throws IOException {
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
  protected RevCommit initContent() throws IOException {
    writeToCache("existing_file.txt");
    commitToMaster();
    writeToCache("some_other_file.txt");
    RevCommit head = commitToMaster();
    clearCache();
    return head;
  }

  @Nonnull
  protected RevCommit initRepository(boolean memory, boolean bare) throws IOException {
    if(!memory)
      initRepositoryDir();
    repo = memory ? new TestRepository(bare) : RepositoryUtils.createRepository(repoDir, bare);
    cache = DirCache.newInCore();
    return initContent();
  }

  @Nonnull
  protected RevCommit initFileRepository(boolean bare) throws IOException {
    return initRepository(false, bare);
  }

  @Nonnull
  protected RevCommit initMemoryRepository(boolean bare) throws IOException {
    return initRepository(true, bare);
  }

  @Nonnull
  protected RevCommit initRepository() throws IOException {
    return initRepository(true, true);
  }

  public static void assertCacheEquals(@Nullable String message, @Nonnull DirCache expected, @Nonnull DirCache actual) {
    if(expected != actual) {
      String header = message == null ? "" : message + ": ";
      int cacheSize = assertCacheSameSize(expected, actual, header);
      DirCacheEntry[] expectedEntries = expected.getEntriesWithin("");
      DirCacheEntry[] actualEntries = actual.getEntriesWithin("");
      for(int i = 0; i < cacheSize; ++i) {
        DirCacheEntry expectedEntry = expectedEntries[i];
        DirCacheEntry actualEntry = actualEntries[i];
        assertCacheEntryEquals(expectedEntry, actualEntry, header, i);
      }
    }
  }

  public static void assertCacheEquals(@Nonnull DirCache expected, @Nonnull DirCache actual) {
    assertCacheEquals(null, expected, actual);
  }

  private static int assertCacheSameSize(@Nonnull DirCache expected, @Nonnull DirCache actual, @Nonnull String header) {
    int actualSize = actual.getEntryCount();
    int expectedSize = expected.getEntryCount();
    if(actualSize != expectedSize)
      Assert.fail(header + "cache sizes differed, expected.size=" + expectedSize + " actual.size=" + actualSize);
    return expectedSize;
  }

  private static void assertCacheEntryEquals(@Nonnull DirCacheEntry expected, @Nonnull DirCacheEntry actual, @Nonnull String header, int index) {
    try {
      Assert.assertEquals("fileMode", expected.getFileMode(), actual.getFileMode());
      Assert.assertEquals("length", expected.getLength(), actual.getLength());
      Assert.assertEquals("objectId", expected.getObjectId(), actual.getObjectId());
      Assert.assertEquals("stage", expected.getStage(), actual.getStage());
      Assert.assertEquals("path", expected.getPathString(), actual.getPathString());
    } catch(AssertionError e) {
      Assert.fail(header + "caches first differed at entry [" + index + "]; " + e.getMessage());
    }
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
