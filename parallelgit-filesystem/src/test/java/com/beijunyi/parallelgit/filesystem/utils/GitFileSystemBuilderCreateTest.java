package com.beijunyi.parallelgit.filesystem.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GitFileSystemBuilderCreateTest extends AbstractParallelGitTest {

  @Before
  public void setupRepository() throws IOException {
    initRepositoryDir();
  }

  @Test
  public void buildWithCreateOption() throws IOException {
    GitFileSystem gfs = GitFileSystemBuilder.prepare()
                          .repository(repoDir)
                          .create()
                          .build();
    Assert.assertEquals(repoDir, gfs.getRepository().getDirectory());
  }

  @Test
  public void buildWithCreateAndBareOption() throws IOException {
    GitFileSystem gfs = GitFileSystemBuilder.prepare()
                          .repository(repoDir)
                          .create()
                          .bare()
                          .build();
    Assert.assertEquals(repoDir, gfs.getRepository().getDirectory());
  }

  @Test
  public void buildWithCreateAndBareOption_nonBare() throws IOException {
    GitFileSystem gfs = GitFileSystemBuilder.prepare()
                          .repository(repoDir)
                          .create()
                          .bare(false)
                          .build();
    Assert.assertEquals(repoDir, gfs.getRepository().getWorkTree());
  }

}
