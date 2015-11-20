package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;

import org.junit.Test;

import static org.junit.Assert.*;

public class GfsFileStoreFileAttributeViewTest extends PreSetupGitFileSystemTest {

  @Test
  public void supportsBasicFileAttributeView_shouldReturnTrue() {
    assertTrue(store.supportsFileAttributeView(BasicFileAttributeView.class));
    assertTrue(store.supportsFileAttributeView("basic"));
  }

  @Test
  public void supportsPosixFileAttributeView_shouldReturnTrue() {
    assertTrue(store.supportsFileAttributeView(PosixFileAttributeView.class));
    assertTrue(store.supportsFileAttributeView("posix"));
  }

  @Test
  public void supportsUnsupportedFileAttributeView_shouldReturnFalse() {
    assertFalse(store.supportsFileAttributeView("unsupported_view"));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void fileStoreGetUnsupportedAttributeTest() throws IOException {
    store.getAttribute("unsupported_attribute");
  }

}
