package com.beijunyi.parallelgit.gfs;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GitFileSystemBasicPropertiesTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
    preventDestroyFileSystem();
  }

  @Test
  public void gitFileSystemReadOnlyTest() {
    Assert.assertFalse(gfs.isReadOnly());
  }

  @Test
  public void gitFileSystemSeparatorTest() {
    Assert.assertEquals("/" , gfs.getSeparator());
  }

  @Test
  public void gitFileSystemRootDirectoriesTest() {
    Iterable<Path> roots = gfs.getRootDirectories();
    Iterator<Path> it = roots.iterator();
    Assert.assertEquals(root, it.next());
    Assert.assertFalse(it.hasNext());
  }

  @Test
  public void gitFileSystemFileStoresTest() {
    Iterable<FileStore> roots = gfs.getFileStores();
    Iterator<FileStore> it = roots.iterator();
    Assert.assertEquals(gfs.getFileStore(), it.next());
    Assert.assertFalse(it.hasNext());
  }

  @Test
  public void gitFileSystemSupportedFileAttributeViewsTest() {
    Set<String> views = gfs.supportedFileAttributeViews();
    Assert.assertTrue(views.contains("basic"));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void gitFileSystemUserPrincipalLookupServiceTest() {
    gfs.getUserPrincipalLookupService();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void gitFileSystemWatchServiceTest() {
    gfs.newWatchService();
  }
}
