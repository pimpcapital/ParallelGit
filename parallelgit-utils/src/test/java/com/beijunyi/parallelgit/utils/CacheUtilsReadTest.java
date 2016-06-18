package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.junit.Before;
import org.junit.Test;

import static org.eclipse.jgit.lib.FileMode.*;
import static org.junit.Assert.*;

public class CacheUtilsReadTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void findEntryWhenEntryExists_shouldReturnTheIndexOfTheEntry() throws IOException {
    writeToCache("/test_file.txt");
    assertTrue(CacheUtils.findEntry("test_file.txt", cache) >= 0);
  }

  @Test
  public void findEntryWhenEntryExistsUsingAbsolutePath_shouldReturnTheIndexOfTheEntry() throws IOException {
    writeToCache("/test_file.txt");
    assertTrue(CacheUtils.findEntry("test_file.txt", cache) >= 0);
  }

  @Test
  public void findEntryWhenEntryDoesNotExist_shouldReturnNegativeValue() throws IOException {
    assertTrue(CacheUtils.findEntry("test_file.txt", cache) < 0);
  }

  @Test
  public void getEntryWhenEntryExists_shouldReturnTheEntry() throws IOException {
    writeToCache("/test_file.txt");
    assertNotNull(CacheUtils.getEntry("test_file.txt", cache));
  }

  @Test
  public void getEntryWhenEntryExistsUsingAbsolutePath_shouldReturnTheEntry() throws IOException {
    writeToCache("/test_file.txt");
    assertNotNull(CacheUtils.getEntry("test_file.txt", cache));
  }

  @Test
  public void getEntryWhenEntryDoesNotExist_shouldReturnNull() throws IOException {
    assertNull(CacheUtils.getEntry("test_file.txt", cache));
  }

  @Test
  public void testEntryExistWhenEntryExists_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt");
    assertTrue(CacheUtils.entryExists("/test_file.txt", cache));
  }

  @Test
  public void testEntryExistWhenEntryDoesNotExist_shouldReturnFalse() throws IOException {
    assertFalse(CacheUtils.entryExists("/test_file.txt", cache));
  }

  @Test
  public void testIsFileWhenFileExists_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt");
    assertTrue(CacheUtils.isFile("/test_file.txt", cache));
  }

  @Test
  public void testIsFileWhenFileDoesNotExist_shouldReturnFalse() throws IOException {
    assertFalse(CacheUtils.isFile("/test_file.txt", cache));
  }

  @Test
  public void testIsFileWhenDirectoryExists_shouldReturnFalse() throws IOException {
    writeToCache("/test/file.txt");
    assertFalse(CacheUtils.isFile("/test", cache));
  }

  @Test
  public void testIsSymbolicLinkWhenRegularFileExists_shouldReturnFalse() throws IOException {
    writeToCache("/test_file.txt");
    assertFalse(CacheUtils.isSymbolicLink("/test_file.txt", cache));
  }

  @Test
  public void testIsSymbolicLinkWhenSymbolicLinkExists_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt", someBytes(), SYMLINK);
    assertTrue(CacheUtils.isSymbolicLink("/test_file.txt", cache));
  }

  @Test
  public void testIsRegularFileWhenRegularFileExists_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt");
    assertTrue(CacheUtils.isRegularFile("/test_file.txt", cache));
  }

  @Test
  public void testIsRegularFileWhenSymbolicLinkExists_shouldReturnFalse() throws IOException {
    writeToCache("/test_file.txt", someBytes(), SYMLINK);
    assertFalse(CacheUtils.isRegularFile("/test_file.txt", cache));
  }

  @Test
  public void testIsExecutableFileWhenRegularFileExists_shouldReturnFalse() throws IOException {
    writeToCache("/test_file.txt");
    assertFalse(CacheUtils.isExecutableFile("/test_file.txt", cache));
  }

  @Test
  public void testIsExecutableFileWhenExecutableFileExists_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.sh", someBytes(), EXECUTABLE_FILE);
    assertTrue(CacheUtils.isExecutableFile("/test_file.sh", cache));
  }

  @Test
  public void testIsRegularOrExecutableFileWhenRegularFileExists_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt");
    assertTrue(CacheUtils.isRegularOrExecutableFile("/test_file.txt", cache));
  }

  @Test
  public void testIsRegularOrExecutableFileWhenExecutableFileExists_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.sh", someBytes(), EXECUTABLE_FILE);
    assertTrue(CacheUtils.isRegularOrExecutableFile("/test_file.sh", cache));
  }

  @Test
  public void testIsRegularOrExecutableFileWhenSymbolicLinkExists_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt", someBytes(), SYMLINK);
    assertFalse(CacheUtils.isRegularOrExecutableFile("/test_file.txt", cache));
  }

  @Test
  public void testIsNonEmptyDirectoryWhenDirectoryDoesNotExist_shouldReturnFalse() throws IOException {
    assertFalse(CacheUtils.isNonEmptyDirectory("/test_dir", cache));
  }

  @Test
  public void testIsNonEmptyDirectoryWhenFileExists_shouldReturnFalse() throws IOException {
    writeToCache("/test");
    assertFalse(CacheUtils.isNonEmptyDirectory("/test", cache));
  }

  @Test
  public void testIsNonEmptyDirectoryWhenDirectoryExists_shouldReturnTrue() throws IOException {
    writeToCache("/test_dir/file.txt");
    assertTrue(CacheUtils.isNonEmptyDirectory("/test_dir", cache));
  }


}
