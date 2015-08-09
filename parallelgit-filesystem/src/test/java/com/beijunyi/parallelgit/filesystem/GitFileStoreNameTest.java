package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class GitFileStoreNameTest extends AbstractGitFileSystemTest {

  @Test
  public void getName_shouldReturnTheFileSystemSessionId() throws IOException {
    initGitFileSystem();
    Assert.assertEquals(gfs.getSessionId(), gfs.getFileStore().name());
  }

}
