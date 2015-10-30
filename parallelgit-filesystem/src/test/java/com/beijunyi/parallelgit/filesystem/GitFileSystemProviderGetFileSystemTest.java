package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.net.URI;

import com.beijunyi.parallelgit.filesystem.utils.GitUriBuilder;
import org.junit.Test;

import static org.junit.Assert.*;

public class GitFileSystemProviderGetFileSystemTest extends PreSetupGitFileSystemTest {

  @Test
  public void getFileSystemForUri_theResultShouldBeTheCorrespondingFileSystem() throws IOException {
    URI uri = root.toUri();
    GitFileSystem result = provider.getFileSystem(uri);
    assertEquals(gfs, result);
  }

  @Test
  public void getFileSystemForUriWithInvalidSessionId_theResultShouldBeNull() {
    URI uri = GitUriBuilder.fromFileSystem(gfs)
                .sid("some_invalid_sid")
                .build();
    assertNull(provider.getFileSystem(uri));
  }

  @Test
  public void getFileSystemForUriWithNoSessionId_theResultShouldBeNull() {
    URI uri = GitUriBuilder.fromFileSystem(gfs)
                .sid(null)
                .build();
    assertNull(provider.getFileSystem(uri));
  }
}
