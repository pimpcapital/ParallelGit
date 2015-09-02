package com.beijunyi.parallelgit.utils;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.util.FileUtils;
import org.junit.*;

public class RepositoryUtilsTest {

  private File dir;
  private Repository repo;

  @Before
  public void setUp() throws IOException {
    dir = FileUtils.createTempDir(getClass().getSimpleName(), null, null);
  }

  @After
  public void tearDown() throws IOException {
    if(repo != null)
      repo.close();
    FileUtils.delete(dir, FileUtils.RECURSIVE);
  }

  @Test
  public void createNonBareRepository_theResultRepositoryWorkTreeShouldEqualToTheInputDirectory() throws Exception {
    repo = RepositoryUtils.createRepository(dir, false);
    Assert.assertEquals(dir, repo.getWorkTree());
  }

  @Test
  public void createBareRepository_theResultRepositoryDirectoryShouldEqualToTheInputDirectory() throws Exception {
    repo = RepositoryUtils.createRepository(dir, true);
    Assert.assertEquals(dir, repo.getDirectory());
  }

  @Test
  public void createRepository_theResultShouldBeBareRepositoryByDefault() throws Exception {
    repo = RepositoryUtils.createRepository(dir);
    Assert.assertTrue(repo.isBare());
  }


}
