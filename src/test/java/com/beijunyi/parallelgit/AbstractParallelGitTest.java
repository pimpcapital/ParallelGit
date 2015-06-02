package com.beijunyi.parallelgit;

import java.io.File;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.util.*;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.util.FileUtils;
import org.junit.After;

public abstract class AbstractParallelGitTest {
  private boolean keepRepo = true;
  protected File repoDir;
  protected Repository repo;
  protected DirCache cache;

  @After
  public void destroyRepoDir() throws IOException {
    if(!keepRepo) {
      if(cache != null) {
        cache.clear();
        cache = null;
      }
      if(repo != null) {
        repo.close();
        repo = null;
      }
      FileUtils.delete(repoDir, FileUtils.RECURSIVE);
      repoDir = null;
    }
  }

  protected void preventDestroyRepo() {
    keepRepo = true;
  }

  @Nonnull
  protected ObjectId writeFile(@Nonnull String path, @Nonnull byte[] content) throws IOException {
    ObjectId blobId = BlobHelper.insert(repo, content);
    DirCacheHelper.addFile(cache, path, blobId);
    return blobId;
  }

  @Nonnull
  protected ObjectId writeFile(@Nonnull String path, @Nonnull String content) throws IOException {
    return writeFile(path, Constants.encode(content));
  }

  @Nonnull
  protected ObjectId writeFile(@Nonnull String path) throws IOException {
    return writeFile(path, path + "'s unique content");
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
    cache = DirCacheHelper.newCache();
  }

  protected void initRepositoryDir() {
    try {
      repoDir = FileUtils.createTempDir(getClass().getSimpleName(), null, null);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Nonnull
  protected ObjectId initRepository(boolean bare) throws IOException {
    if(repoDir == null)
      initRepositoryDir();
    repo = RepositoryHelper.createRepository(repoDir, bare);
    cache = DirCacheHelper.newCache();
    return commitToMaster();
  }

  @Nonnull
  protected ObjectId initRepository() throws IOException {
    return initRepository(true);
  }

}
