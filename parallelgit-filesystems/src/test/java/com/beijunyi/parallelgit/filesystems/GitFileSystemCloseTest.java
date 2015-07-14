package com.beijunyi.parallelgit.filesystems;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;

import org.junit.Assert;
import org.junit.Test;

public class GitFileSystemCloseTest extends AbstractGitFileSystemTest {

  @Test
  public void closedFileSystemIsOpenTest() throws IOException {
    initGitFileSystem();
    gfs.close();
    Assert.assertFalse(gfs.isOpen());
  }

  @Test
  public void closedFileSystemGetWithUriTest() throws IOException {
    initGitFileSystem();
    URI uri = root.toUri();
    gfs.close();
    Assert.assertNull(FileSystems.getFileSystem(uri));
  }
}
