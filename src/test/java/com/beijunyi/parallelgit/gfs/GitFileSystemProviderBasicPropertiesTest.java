package com.beijunyi.parallelgit.gfs;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GitFileSystemProviderBasicPropertiesTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void gitFileSystemProviderSchemeTest() {
    Assert.assertEquals(GitFileSystemProvider.GIT_FS_SCHEME, gfs.provider().getScheme());
  }

  @Test
  public void gitFileSystemProviderGetFileStoreTest() throws IOException {
    Assert.assertNotNull(Files.getFileStore(root));
    Assert.assertEquals(root.getFileSystem().getFileStore(), Files.getFileStore(root));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void gitFileSystemProviderSetAttributeTest() {
    gfs.provider().setAttribute(null, null, null);
  }
}
