package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GitFileUtilsGetFileAttributesTest extends AbstractParallelGitTest {

  @Before
  public void setupRepository() throws IOException {
    initRepository();
  }

  @Test
  public void testFileExistsWhenFileExists_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt");
    RevCommit commit = commit();
    Assert.assertTrue(GitFileUtils.exists("/test_file.txt", commit.getName(), repo));
  }

  @Test
  public void testFileExistsWhenDirectoryExists_shouldReturnTrue() throws IOException {
    writeToCache("/dir/file.txt");
    RevCommit commit = commit();
    Assert.assertTrue(GitFileUtils.exists("/dir", commit.getName(), repo));
  }

  @Test
  public void testFileExistsWhenFileDoesNotExist_shouldReturnFalse() throws IOException {
    writeSomethingToCache();
    RevCommit commit = commit();
    Assert.assertFalse(GitFileUtils.exists("/non_existent_file.txt", commit.getName(), repo));
  }

  @Test
  public void testIsFileWhenFileExists_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt");
    RevCommit commit = commit();
    Assert.assertTrue(GitFileUtils.isFile("/test_file.txt", commit.getName(), repo));
  }

  @Test
  public void testIsFileWhenDirectoryExists_shouldReturnFalse() throws IOException {
    writeToCache("/dir/file.txt");
    RevCommit commit = commit();
    Assert.assertFalse(GitFileUtils.isFile("/dir", commit.getName(), repo));
  }

  @Test
  public void testIsFileWhenSymbolicLinkExists_shouldReturnFalse() throws IOException {
    writeToCache("/test_file.txt", "some link data".getBytes(), FileMode.SYMLINK);
    RevCommit commit = commit();
    Assert.assertFalse(GitFileUtils.isFile("/test_file", commit.getName(), repo));
  }

  @Test
  public void testIsDirectoryWhenFileExists_shouldReturnFalse() throws IOException {
    writeToCache("/test_file.txt");
    RevCommit commit = commit();
    Assert.assertFalse(GitFileUtils.isDirectory("/test_file.txt", commit.getName(), repo));
  }

  @Test
  public void testIsDirectoryWhenDirectoryExists_shouldReturnFalse() throws IOException {
    writeToCache("/dir/file.txt");
    RevCommit commit = commit();
    Assert.assertTrue(GitFileUtils.isDirectory("/dir", commit.getName(), repo));
  }

  @Test
  public void testIsSymbolicLinkWhenFileExists_shouldReturnFalse() throws IOException {
    writeToCache("/test_file.txt");
    RevCommit commit = commit();
    Assert.assertFalse(GitFileUtils.isSymbolicLink("/test_file.txt", commit.getName(), repo));
  }

  @Test
  public void testIsSymbolicLinkWhenSymbolicLinkExists_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt", "some link data".getBytes(), FileMode.SYMLINK);
    RevCommit commit = commit();
    Assert.assertTrue(GitFileUtils.isSymbolicLink("/test_file.txt", commit.getName(), repo));
  }

}
