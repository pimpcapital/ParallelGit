package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.net.URI;

import org.junit.Test;

import static org.junit.Assert.*;

public class GitFileSystemCloseTest extends PreSetupGitFileSystemTest {

  @Test
  public void closedFileSystemIsOpenTest() throws IOException {
    gfs.close();
    assertFalse(gfs.isOpen());
  }

  @Test
  public void closedFileSystemGetWithUriTest() throws IOException {
    URI uri = root.toUri();
    gfs.close();
    assertNull(provider.getFileSystem(uri));
  }
}
