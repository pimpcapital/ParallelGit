package com.beijunyi.parallelgit.gfs;

import java.nio.file.attribute.BasicFileAttributeView;

import org.junit.Assert;
import org.junit.Test;

public class GitFileStoreBasicPropertiesTest extends AbstractGitFileSystemTest {

  @Test
  public void fileStoreReadOnlyTest() {
    initGitFileSystem();
    GitFileStore store = gfs.getFileStore();
    Assert.assertFalse(store.isReadOnly());
  }

  @Test
  public void fileStoreSpaceTest() {
    initGitFileSystem();
    GitFileStore store = gfs.getFileStore();
    Assert.assertEquals(repoDir.getTotalSpace(), store.getTotalSpace());
    Assert.assertEquals(repoDir.getUsableSpace(), store.getUsableSpace());
    Assert.assertEquals(repoDir.getFreeSpace(), store.getUnallocatedSpace());
  }

  @Test
  public void fileStoreFileSupportsAttributeViewTest() {
    initGitFileSystem();
    GitFileStore store = gfs.getFileStore();
    Assert.assertTrue(store.supportsFileAttributeView(BasicFileAttributeView.class));
    Assert.assertTrue(store.supportsFileAttributeView("basic"));
  }

  @Test
  public void fileStoreGetAttributeTest() {
    initGitFileSystem();
    GitFileStore store = gfs.getFileStore();
    Assert.assertEquals(repoDir.getTotalSpace(), store.getAttribute("totalSpace"));
    Assert.assertEquals(repoDir.getUsableSpace(), store.getAttribute("usableSpace"));
    Assert.assertEquals(repoDir.getFreeSpace(), store.getAttribute("unallocatedSpace"));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void fileStoreGetUnsupportedAttributeTest() {
    initGitFileSystem();
    GitFileStore store = gfs.getFileStore();
    store.getAttribute("custom_attribute");
  }

}
