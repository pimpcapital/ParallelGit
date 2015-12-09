package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;

import org.junit.Test;

import static org.junit.Assert.*;

public class GfsFileStoreFileAttributeViewTest extends PreSetupGitFileSystemTest {

  @Test
  public void supportsBasicFileAttributeView_shouldReturnTrue() {
    assertTrue(fileStore.supportsFileAttributeView(BasicFileAttributeView.class));
    assertTrue(fileStore.supportsFileAttributeView("basic"));
  }

  @Test
  public void supportsPosixFileAttributeView_shouldReturnTrue() {
    assertTrue(fileStore.supportsFileAttributeView(PosixFileAttributeView.class));
    assertTrue(fileStore.supportsFileAttributeView("posix"));
  }

  @Test
  public void supportsUnsupportedFileAttributeView_shouldReturnFalse() {
    assertFalse(fileStore.supportsFileAttributeView("unsupported_view"));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void fileStoreGetUnsupportedAttributeTest() throws IOException {
    fileStore.getAttribute("unsupported_attribute");
  }

}
