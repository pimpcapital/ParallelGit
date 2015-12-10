package com.beijunyi.parallelgit.filesystem.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GfsConfigurationCreateNewRepositoryTest extends AbstractParallelGitTest {

  @Before
  public void setupRepository() throws IOException {
    initRepositoryDir();
  }

  @Test
  public void buildWithCreateOption() throws IOException {
    GitFileSystem gfs = Gfs.newFileSystem()
                          .repository(repoDir)
                          .create()
                          .build();
    assertEquals(repoDir, gfs.getRepository().getDirectory());
  }

  @Test
  public void buildWithCreateAndBareOption() throws IOException {
    GitFileSystem gfs = Gfs.newFileSystem()
                          .repository(repoDir)
                          .create()
                          .bare()
                          .build();
    assertEquals(repoDir, gfs.getRepository().getDirectory());
  }

  @Test
  public void buildWithCreateAndBareOption_nonBare() throws IOException {
    GitFileSystem gfs = Gfs.newFileSystem(repoDir)
                          .create()
                          .bare(false)
                          .build();
    assertEquals(repoDir, gfs.getRepository().getWorkTree());
  }

}
