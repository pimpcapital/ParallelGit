package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystemNotFoundException;

import com.beijunyi.parallelgit.filesystem.utils.GfsUriBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GitFileSystemProviderGetFileSystemTest extends PreSetupGitFileSystemTest {

  @Test
  public void getFileSystemForUri_theResultShouldBeTheCorrespondingFileSystem() throws IOException {
    URI uri = root.toUri();
    GitFileSystem result = provider.getFileSystem(uri);
    assertEquals(gfs, result);
  }

  @Test(expected = FileSystemNotFoundException.class)
  public void getFileSystemForUriWithInvalidSessionId_shouldThrowFileSystemNotFoundException() {
    URI uri = GfsUriBuilder.fromFileSystem(gfs)
                .sid("some_invalid_sid")
                .build();
    provider.getFileSystem(uri);
  }

  @Test(expected = FileSystemNotFoundException.class)
  public void getFileSystemForUriWithNoSessionId_shouldThrowFileSystemNotFoundException() {
    URI uri = GfsUriBuilder.fromFileSystem(gfs)
                .sid(null)
                .build();
    provider.getFileSystem(uri);
  }
}
