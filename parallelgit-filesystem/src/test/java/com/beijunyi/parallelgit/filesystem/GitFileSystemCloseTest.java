package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystemNotFoundException;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class GitFileSystemCloseTest extends PreSetupGitFileSystemTest {

  @Test
  public void closeFileSystem_fileSystemShouldBecomeClosed() throws IOException {
    gfs.close();
    assertFalse(gfs.isOpen());
  }

  @Test(expected = FileSystemNotFoundException.class)
  public void closeFileSystemAndGetFileSystem_shouldThrowFileSystemNotFoundException() throws IOException {
    URI uri = root.toUri();
    gfs.close();
    provider.getFileSystem(uri);
  }
}
