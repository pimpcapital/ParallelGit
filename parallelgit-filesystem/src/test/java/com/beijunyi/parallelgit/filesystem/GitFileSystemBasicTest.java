package com.beijunyi.parallelgit.filesystem;

import java.nio.file.FileStore;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.*;

public class GitFileSystemBasicTest extends PreSetupGitFileSystemTest {

  @Test
  public void testIsReadOnly_shouldReturnFalse() {
    assertFalse(gfs.isReadOnly());
  }

  @Test
  public void getSeparator_shouldReturnForwardSlash() {
    assertEquals("/", gfs.getSeparator());
  }

  @Test
  public void getRootDirectories_shouldReturnOneRoot() {
    Collection<Path> roots = new ArrayList<>();
    for(Path root : gfs.getRootDirectories())
      roots.add(root);
    assertEquals(1, roots.size());
  }

  @Test
  public void getRootDirectories_shouldContainTheOnlyRootPath() {
    Collection<Path> roots = new ArrayList<>();
    for(Path root : gfs.getRootDirectories())
      roots.add(root);
    assertTrue(roots.contains(gfs.getRootPath()));
  }

  @Test
  public void getFileStores_shouldReturnOneFileStore() {
    Collection<FileStore> stores = new ArrayList<>();
    for(FileStore root : gfs.getFileStores())
      stores.add(root);
    assertEquals(1, stores.size());
  }

  @Test
  public void getFileStores_shouldContainTheOnlyFileStore() {
    Collection<FileStore> stores = new ArrayList<>();
    for(FileStore root : gfs.getFileStores())
      stores.add(root);
    assertTrue(stores.contains(gfs.getFileStore()));
  }

  @Test
  public void getSupportedFileAttributeViews_shouldContainBasic() {
    Set<String> views = gfs.supportedFileAttributeViews();
    assertTrue(views.contains("basic"));
  }

  @Test
  public void getSupportedFileAttributeViews_shouldContainPosix() {
    Set<String> views = gfs.supportedFileAttributeViews();
    assertTrue(views.contains("posix"));
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
