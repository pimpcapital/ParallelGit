package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;

import org.junit.Before;

public abstract class PreSetupGitFileSystemTest extends AbstractGitFileSystemTest {

  protected GfsFileStore fileStore;
  protected GfsStatusProvider statusProvider;

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
    fileStore = gfs.getFileStore();
    statusProvider = gfs.getStatusProvider();
  }

}
