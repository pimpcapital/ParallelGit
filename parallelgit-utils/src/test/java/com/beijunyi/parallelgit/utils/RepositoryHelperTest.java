package com.beijunyi.parallelgit.utils;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.util.FileUtils;
import org.junit.*;

public class RepositoryHelperTest {

  private Repository repo;
  private File repoDir;

  @Before
  public void setUp() throws IOException {
    repoDir = FileUtils.createTempDir(getClass().getSimpleName(), null, null);
  }

  @After
  public void tearDown() throws IOException {
    if(repo != null)
      repo.close();
    FileUtils.delete(repoDir, FileUtils.RECURSIVE);
  }

  @Test
  public void testCreateRepository() throws Exception {
    repo = RepositoryHelper.createRepository(repoDir, false);
    Assert.assertEquals(repoDir, repo.getWorkTree());
    Assert.assertEquals(new File(repoDir, Constants.DOT_GIT), repo.getDirectory());
  }

  @Test
  public void testCreateBareRepository() throws Exception {
    repo = RepositoryHelper.createRepository(repoDir, true);
    Assert.assertEquals(repoDir, repo.getDirectory());
  }
}
