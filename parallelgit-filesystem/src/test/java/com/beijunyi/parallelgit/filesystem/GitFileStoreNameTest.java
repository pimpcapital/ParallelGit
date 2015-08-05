package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class GitFileStoreNameTest extends AbstractGitFileSystemTest {

  @Test
  public void getName_shouldReturnTheRepositoryDirectoryPath() throws IOException {
    initGitFileSystem();
    Assert.assertEquals(repoDir.getAbsolutePath(), gfs.getFileStore().name());
  }

}
