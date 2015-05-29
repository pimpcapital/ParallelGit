package com.beijunyi.parallelgit.gfs;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GitFileSystemProviderGetPathTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
    preventDestroyFileSystem();
  }

  @Test
  public void getPathWithExistingSessionIdTest() {
    URI uri = GitUriUtils.createUri(repoDir, "/some_file.txt", gfs.getSessionId(), null, null, null, null, null);
    GitPath path = (GitPath) Paths.get(uri);
    Assert.assertEquals(gfs.getPath("/some_file.txt"), path);
  }

  @Test
  public void getRootPathWithExistingSessionIdTest() {
    URI uri = GitUriUtils.createUri(repoDir, "/", gfs.getSessionId(), null, null, null, null, null);
    GitPath path = (GitPath) Paths.get(uri);
    Assert.assertEquals(root, path);
  }

  @Test
  public void getPathWithNoSessionIdSpecifiedTest() {
    URI uri = GitUriUtils.createUri(repoDir, "/some_file.txt", null, null, null, null, null, null);
    GitPath path = (GitPath) Paths.get(uri);
    Assert.assertNotNull(path);
    Assert.assertNotNull(path.getFileSystem().getSessionId());
  }

  @Test
  public void getPathWithNewSessionIdTest() {
    URI uri = GitUriUtils.createUri(repoDir, "/some_file.txt", "new_session", null, null, null, null, null);
    GitPath path = (GitPath) Paths.get(uri);
    Assert.assertNotNull(path);
    Assert.assertEquals("new_session", path.getFileSystem().getSessionId());
  }
}
