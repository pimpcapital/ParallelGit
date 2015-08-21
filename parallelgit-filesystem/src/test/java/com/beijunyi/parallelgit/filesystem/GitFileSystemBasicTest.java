package com.beijunyi.parallelgit.filesystem;

import java.nio.file.FileStore;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class GitFileSystemBasicTest extends PreSetupGitFileSystemTest {

  @Test
  public void testIsReadOnly_shouldReturnFalse() {
    Assert.assertFalse(gfs.isReadOnly());
  }

  @Test
  public void getSeparator_shouldReturnForwardSlash() {
    Assert.assertEquals("/" , gfs.getSeparator());
  }

  @Test
  public void getRootDirectories_shouldReturnOneRoot() {
    Collection<Path> roots = new ArrayList<>();
    for(Path root : gfs.getRootDirectories())
      roots.add(root);
    Assert.assertEquals(1, roots.size());
  }

  @Test
  public void getRootDirectories_shouldContainTheOnlyRootPath() {
    Collection<Path> roots = new ArrayList<>();
    for(Path root : gfs.getRootDirectories())
      roots.add(root);
    Assert.assertTrue(roots.contains(gfs.getRootPath()));
  }

  @Test
  public void getFileStores_shouldReturnOneFileStore() {
    Collection<FileStore> stores = new ArrayList<>();
    for(FileStore root : gfs.getFileStores())
      stores.add(root);
    Assert.assertEquals(1, stores.size());
  }

  @Test
  public void getFileStores_shouldContainTheOnlyFileStore() {
    Collection<FileStore> stores = new ArrayList<>();
    for(FileStore root : gfs.getFileStores())
      stores.add(root);
    Assert.assertTrue(stores.contains(gfs.getFileStore()));
  }

  @Test
  public void getSupportedFileAttributeViews_shouldContainBasic() {
    Set<String> views = gfs.supportedFileAttributeViews();
    Assert.assertTrue(views.contains("basic"));
  }

  @Test
  public void getSupportedFileAttributeViews_shouldContainPosix() {
    Set<String> views = gfs.supportedFileAttributeViews();
    Assert.assertTrue(views.contains("posix"));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getUserPrincipalLookupService_shouldThrowUnsupportedOperationException() {
    gfs.getUserPrincipalLookupService();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void newWatchService_shouldThrowUnsupportedOperationException() {
    gfs.newWatchService();
  }
}
