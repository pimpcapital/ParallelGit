package com.beijunyi.parallelgit.utils;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class RepositoryUtilsOpenRepositoryTest {

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
  public void openBareRepository_theResultRepositoryDirectoryShouldEqualToTheInputDirectory() throws IOException {
    RepositoryUtils.createRepository(dir, true);
    repo = RepositoryUtils.openRepository(dir, true);
    assertEquals(dir, repo.getDirectory());
  }

  @Test
  public void openNonBareRepository_theResultRepositoryWorkTreeShouldEqualToTheInputDirectory() throws Exception {
    RepositoryUtils.createRepository(dir, false);

    repo = RepositoryUtils.openRepository(dir, false);
    assertEquals(dir, repo.getWorkTree());
  }

  @Test
  public void autoDetectAndOpenBareRepository_theResultRepositoryDirectoryShouldEqualToTheInputDirectory() throws IOException {
    RepositoryUtils.createRepository(dir, true);
    repo = RepositoryUtils.openRepository(dir);
    assertEquals(dir, repo.getDirectory());
  }

  @Test
  public void autoDetectAndOpenNonBareRepository_theResultRepositoryWorkTreeShouldEqualToTheInputDirectory() throws Exception {
    RepositoryUtils.createRepository(dir, false);
    repo = RepositoryUtils.openRepository(dir);
    assertEquals(dir, repo.getWorkTree());
  }

}
