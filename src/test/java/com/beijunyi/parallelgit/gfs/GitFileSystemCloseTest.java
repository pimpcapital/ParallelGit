package com.beijunyi.parallelgit.gfs;

import java.net.URI;
import java.nio.file.FileSystems;

import org.junit.Assert;
import org.junit.Test;

public class GitFileSystemCloseTest extends AbstractGitFileSystemTest {

  @Test
  public void closedFileSystemIsOpenTest() {
    initGitFileSystem();
    String sessionId = gfs.getSessionId();
    URI uri = root.toUri();
    gfs.close();
    Assert.assertNull(GitFileSystems.getFileSystem(sessionId));
    Assert.assertNull(FileSystems.getFileSystem(uri));
  }

  @Test
  public void closedFileSystemFileStoreIsOpenTest() {
    initGitFileSystem();
    GitFileStore store = gfs.getFileStore();
    gfs.close();
    Assert.assertFalse(store.isOpen());
  }

  @Test
  public void closedFileSystemGetWithSessionIdTest() {
    initGitFileSystem();
    String sessionId = gfs.getSessionId();
    gfs.close();
    Assert.assertNull(GitFileSystems.getFileSystem(sessionId));
  }

  @Test
  public void closedFileSystemGetWithUriTest() {
    initGitFileSystem();
    URI uri = root.toUri();
    gfs.close();
    Assert.assertNull(FileSystems.getFileSystem(uri));
  }
}
