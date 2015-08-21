package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

public class GitFileSystemCloseTest extends PreSetupGitFileSystemTest {

  @Test
  public void closedFileSystemIsOpenTest() throws IOException {
    gfs.close();
    Assert.assertFalse(gfs.isOpen());
  }

  @Test
  public void closedFileSystemGetWithUriTest() throws IOException {
    URI uri = root.toUri();
    gfs.close();
    Assert.assertNull(provider.getFileSystem(uri));
  }
}
