package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;

import org.junit.Assert;
import org.junit.Test;

public class GitFileStoreFileAttributeViewTest extends PreSetupGitFileSystemTest {

  @Test
  public void supportsBasicFileAttributeView_shouldReturnTrue() {
    Assert.assertTrue(store.supportsFileAttributeView(BasicFileAttributeView.class));
    Assert.assertTrue(store.supportsFileAttributeView("basic"));
  }

  @Test
  public void supportsPosixFileAttributeView_shouldReturnTrue() {
    Assert.assertTrue(store.supportsFileAttributeView(PosixFileAttributeView.class));
    Assert.assertTrue(store.supportsFileAttributeView("posix"));
  }

  @Test
  public void supportsUnsupportedFileAttributeView_shouldReturnFalse() {
    Assert.assertFalse(store.supportsFileAttributeView("unsupported_view"));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void fileStoreGetUnsupportedAttributeTest() throws IOException {
    store.getAttribute("unsupported_attribute");
  }

}
