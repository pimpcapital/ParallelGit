package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;

import org.junit.Assert;
import org.junit.Test;

public class GitFileStoreFileAttributeViewTest extends AbstractGitFileSystemTest {

  @Test
  public void supportsBasicFileAttributeView_shouldReturnTrue() throws IOException {
    initGitFileSystem();
    GitFileStore store = gfs.getFileStore();
    Assert.assertTrue(store.supportsFileAttributeView(BasicFileAttributeView.class));
    Assert.assertTrue(store.supportsFileAttributeView("basic"));
  }

  @Test
  public void supportsPosixFileAttributeView_shouldReturnTrue() throws IOException {
    initGitFileSystem();
    GitFileStore store = gfs.getFileStore();
    Assert.assertTrue(store.supportsFileAttributeView(PosixFileAttributeView.class));
    Assert.assertTrue(store.supportsFileAttributeView("posix"));
  }

  @Test
  public void supportsUnsupportedFileAttributeView_shouldReturnFalse() throws IOException {
    initGitFileSystem();
    GitFileStore store = gfs.getFileStore();
    Assert.assertFalse(store.supportsFileAttributeView("unsupported_view"));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void fileStoreGetUnsupportedAttributeTest() throws IOException {
    initGitFileSystem();
    GitFileStore store = gfs.getFileStore();
    store.getAttribute("unsupported_attribute");
  }

}
