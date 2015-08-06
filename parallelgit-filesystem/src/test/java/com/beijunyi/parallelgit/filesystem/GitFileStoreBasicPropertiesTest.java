package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;

import org.junit.Assert;
import org.junit.Test;

public class GitFileStoreBasicPropertiesTest extends AbstractGitFileSystemTest {

  @Test
  public void fileStoreFileSupportsAttributeViewTest() throws IOException {
    Path zipRoot = FileSystems.newFileSystem(Paths.get("e:/e.zip"), null).getRootDirectories().iterator().next();
    Files.delete(zipRoot);

    initGitFileSystem();
    GitFileStore store = gfs.getFileStore();
    Assert.assertTrue(store.supportsFileAttributeView(BasicFileAttributeView.class));
    Assert.assertTrue(store.supportsFileAttributeView("basic"));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void fileStoreGetUnsupportedAttributeTest() throws IOException {
    initGitFileSystem();
    GitFileStore store = gfs.getFileStore();
    store.getAttribute("custom_attribute");
  }

}
