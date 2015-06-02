package com.beijunyi.parallelgit.util;

import java.io.File;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.Constants;
import org.junit.Assert;
import org.junit.Test;

public class RepositoryHelperTest extends AbstractParallelGitTest {

  @Test
  public void testCreateRepository() throws Exception {
    initRepositoryDir();
    repo = RepositoryHelper.createRepository(repoDir, false);
    Assert.assertEquals(repoDir, repo.getWorkTree());
    Assert.assertEquals(new File(repoDir, Constants.DOT_GIT), repo.getDirectory());
  }

  @Test
  public void testCreateBareRepository() throws Exception {
    initRepositoryDir();
    repo = RepositoryHelper.createRepository(repoDir, true);
    Assert.assertEquals(repoDir, repo.getDirectory());
  }
}
