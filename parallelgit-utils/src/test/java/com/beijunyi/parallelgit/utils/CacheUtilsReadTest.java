package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.FileMode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CacheUtilsReadTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void findEntryWhenEntryExists_shouldReturnTheIndexOfTheEntry() throws IOException {
    writeToCache("/test_file.txt");
    Assert.assertTrue(CacheUtils.findEntry("test_file.txt", cache) >= 0);
  }

  @Test
  public void findEntryWhenEntryExistsUsingAbsolutePath_shouldReturnTheIndexOfTheEntry() throws IOException {
    writeToCache("/test_file.txt");
    Assert.assertTrue(CacheUtils.findEntry("test_file.txt", cache) >= 0);
  }

  @Test
  public void findEntryWhenEntryDoesNotExist_shouldReturnNegativeValue() throws IOException {
    Assert.assertTrue(CacheUtils.findEntry("test_file.txt", cache) < 0);
  }

  @Test
  public void getEntryWhenEntryExists_shouldReturnTheEntry() throws IOException {
    writeToCache("/test_file.txt");
    Assert.assertNotNull(CacheUtils.getEntry("test_file.txt", cache));
  }

  @Test
  public void getEntryWhenEntryExistsUsingAbsolutePath_shouldReturnTheEntry() throws IOException {
    writeToCache("/test_file.txt");
    Assert.assertNotNull(CacheUtils.getEntry("test_file.txt", cache));
  }

  @Test
  public void getEntryWhenEntryDoesNotExist_shouldReturnNull() throws IOException {
    Assert.assertNull(CacheUtils.getEntry("test_file.txt", cache));
  }

  @Test
  public void testEntryExistWhenEntryExists_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt");
    Assert.assertTrue(CacheUtils.entryExists("/test_file.txt", cache));
  }

  @Test
  public void testEntryExistWhenEntryDoesNotExist_shouldReturnFalse() throws IOException {
    Assert.assertFalse(CacheUtils.entryExists("/test_file.txt", cache));
  }

  @Test
  public void testIsFileWhenFileExists_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt");
    Assert.assertTrue(CacheUtils.isFile("/test_file.txt", cache));
  }

  @Test
  public void testIsFileWhenFileDoesNotExist_shouldReturnFalse() throws IOException {
    Assert.assertFalse(CacheUtils.isFile("/test_file.txt", cache));
  }

  @Test
  public void testIsFileWhenDirectoryExists_shouldReturnFalse() throws IOException {
    writeToCache("/test/file.txt");
    Assert.assertFalse(CacheUtils.isFile("/test", cache));
  }

  @Test
  public void testIsSymbolicLinkWhenRegularFileExists_shouldReturnFalse() throws IOException {
    writeToCache("/test_file.txt");
    Assert.assertFalse(CacheUtils.isSymbolicLink("/test_file.txt", cache));
  }

  @Test
  public void testIsSymbolicLinkWhenSymbolicLinkExists_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt", "some link data".getBytes(), FileMode.SYMLINK);
    Assert.assertTrue(CacheUtils.isSymbolicLink("/test_file.txt", cache));
  }

  @Test
  public void testIsRegularFileWhenRegularFileExists_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt");
    Assert.assertTrue(CacheUtils.isRegularFile("/test_file.txt", cache));
  }

  @Test
  public void testIsRegularFileWhenSymbolicLinkExists_shouldReturnFalse() throws IOException {
    writeToCache("/test_file.txt", "some link data".getBytes(), FileMode.SYMLINK);
    Assert.assertFalse(CacheUtils.isRegularFile("/test_file.txt", cache));
  }

  @Test
  public void testIsExecutableFileWhenRegularFileExists_shouldReturnFalse() throws IOException {
    writeToCache("/test_file.txt");
    Assert.assertFalse(CacheUtils.isExecutableFile("/test_file.txt", cache));
  }

  @Test
  public void testIsExecutableFileWhenExecutableFileExists_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.sh", "some executable data".getBytes(), FileMode.EXECUTABLE_FILE);
    Assert.assertTrue(CacheUtils.isExecutableFile("/test_file.sh", cache));
  }

  @Test
  public void testIsRegularOrExecutableFileWhenRegularFileExists_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt");
    Assert.assertTrue(CacheUtils.isRegularOrExecutableFile("/test_file.txt", cache));
  }

  @Test
  public void testIsRegularOrExecutableFileWhenExecutableFileExists_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.sh", "some executable data".getBytes(), FileMode.EXECUTABLE_FILE);
    Assert.assertTrue(CacheUtils.isRegularOrExecutableFile("/test_file.sh", cache));
  }

  @Test
  public void testIsRegularOrExecutableFileWhenSymbolicLinkExists_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt", "some link data".getBytes(), FileMode.SYMLINK);
    Assert.assertFalse(CacheUtils.isRegularOrExecutableFile("/test_file.txt", cache));
  }

  @Test
  public void testIsNonEmptyDirectoryWhenDirectoryDoesNotExist_shouldReturnFalse() throws IOException {
    Assert.assertFalse(CacheUtils.isNonEmptyDirectory("/test_dir", cache));
  }

  @Test
  public void testIsNonEmptyDirectoryWhenFileExists_shouldReturnFalse() throws IOException {
    writeToCache("/test");
    Assert.assertFalse(CacheUtils.isNonEmptyDirectory("/test", cache));
  }

  @Test
  public void testIsNonEmptyDirectoryWhenDirectoryExists_shouldReturnTrue() throws IOException {
    writeToCache("/test_dir/file.txt");
    Assert.assertTrue(CacheUtils.isNonEmptyDirectory("/test_dir", cache));
  }


}
