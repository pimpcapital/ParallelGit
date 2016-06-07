package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.internal.storage.dfs.DfsRepository;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.*;
import org.junit.Before;
import org.junit.Test;

public class RepositoryUtilsGarbageCollectTest extends AbstractParallelGitTest {

  @Before
  public void setUpRepoDir() throws IOException {
    initRepositoryDir();
  }

  @Test
  public void garbageCollectFileRepository_shouldProceedTheOperation() throws IOException {
    FileRepository repo = new FileRepository(repoDir);
    RepositoryUtils.garbageCollect(repo);
  }

  @Test
  public void garbageCollectDfsRepository_shouldProceedTheOperation() throws IOException {
    DfsRepository repo = new TestRepository();
    RepositoryUtils.garbageCollect(repo);
  }

  @Test
  public void autoDetectAndGarbageCollectFileRepository_shouldProceedTheOperation() throws IOException {
    Repository repo = new FileRepository(repoDir);
    RepositoryUtils.garbageCollect(repo);
  }

  @Test
  public void autoDetectAndGarbageCollectDfsRepository_shouldProceedTheOperation() throws IOException {
    Repository repo = new TestRepository();
    RepositoryUtils.garbageCollect(repo);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void garbageCollectUnsupportedRepository_shouldThrowUnsupportedOperation() throws IOException {
    RepositoryUtils.garbageCollect(new Repository(new BaseRepositoryBuilder()) {
      @Override
      public void create(boolean bare) throws IOException {}

      @Nullable
      @Override
      public ObjectDatabase getObjectDatabase() {
        return null;
      }

      @Nullable
      @Override
      public RefDatabase getRefDatabase() {
        return null;
      }

      @Nullable
      @Override
      public StoredConfig getConfig() {
        return null;
      }

      @Override
      public void scanForRepoChanges() throws IOException {
      }

      @Override
      public void notifyIndexChanged() {
      }

      @Nullable
      @Override
      public ReflogReader getReflogReader(String refName) throws IOException {
        return null;
      }
    });
  }
}
