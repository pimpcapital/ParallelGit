package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.net.URI;

import com.beijunyi.parallelgit.filesystem.utils.GitUriBuilder;
import org.junit.Assert;
import org.junit.Test;

public class GitFileSystemProviderGetFileSystemTest extends PreSetupGitFileSystemTest {

  @Test
  public void getFileSystemForUri_theResultShouldBeTheCorrespondingFileSystem() throws IOException {
    URI uri = root.toUri();
    GitFileSystem result = provider.getFileSystem(uri);
    Assert.assertEquals(gfs, result);
  }

  @Test
  public void getFileSystemForUriWithInvalidSessionId_theResultShouldBeNull() {
    URI uri = GitUriBuilder.forFileSystem(gfs)
                .sid("some_invalid_sid")
                .build();
    Assert.assertNull(provider.getFileSystem(uri));
  }

  @Test
  public void getFileSystemForUriWithNoSessionId_theResultShouldBeNull() {
    URI uri = GitUriBuilder.forFileSystem(gfs)
                .sid(null)
                .build();
    Assert.assertNull(provider.getFileSystem(uri));
  }
}
