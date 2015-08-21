package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;

import org.junit.Before;

public abstract class PreSetupGitFileSystemTest extends AbstractGitFileSystemTest {

  protected GitFileStore store;

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
    store = gfs.getFileStore();
  }

}
