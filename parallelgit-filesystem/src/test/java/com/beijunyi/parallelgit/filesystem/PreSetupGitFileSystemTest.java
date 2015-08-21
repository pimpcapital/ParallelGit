package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;

import org.junit.Before;

public abstract class PreSetupGitFileSystemTest extends AbstractGitFileSystemTest {

  protected GitFileStore store;
  protected String branch;

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
    store = gfs.getFileStore();
    branch = gfs.getBranch();
  }

}
