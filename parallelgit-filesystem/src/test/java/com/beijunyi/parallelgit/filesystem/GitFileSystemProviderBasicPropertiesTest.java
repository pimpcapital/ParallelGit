package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GitFileSystemProviderBasicPropertiesTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void gitFileSystemProviderSchemeTest() {
    assertEquals(GitFileSystemProvider.GIT_FS_SCHEME, provider.getScheme());
  }

  @Test
  public void gitFileSystemProviderGetFileStoreTest() throws IOException {
    assertEquals(gfs.getFileStore(), provider.getFileStore(root));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void gitFileSystemProviderSetAttributeTest() {
    gfs.provider().setAttribute(null, null, null);
  }
}
