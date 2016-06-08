package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.internal.storage.dfs.DfsRepository;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
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

}
