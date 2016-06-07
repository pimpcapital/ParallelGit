package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RepositoryUtilsCreateRepositoryTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepositoryDir();
  }

  @Test
  public void createBareRepository_theResultRepositoryDirectoryShouldEqualToTheInputDirectory() throws Exception {
    repo = RepositoryUtils.createRepository(repoDir, true);
    assertEquals(repoDir, repo.getDirectory());
  }

  @Test
  public void createNonBareRepository_theResultRepositoryWorkTreeShouldEqualToTheInputDirectory() throws Exception {
    repo = RepositoryUtils.createRepository(repoDir, false);
    assertEquals(repoDir, repo.getWorkTree());
  }

  @Test
  public void createRepositoryWithoutSpecifyingBareness_theResultShouldBeBareRepository() throws Exception {
    repo = RepositoryUtils.createRepository(repoDir);
    assertTrue(repo.isBare());
  }

}
