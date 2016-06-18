package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;

import static org.eclipse.jgit.lib.FileMode.*;
import static org.junit.Assert.*;

public class GitFileUtilsGetFileAttributesTest extends AbstractParallelGitTest {

  @Before
  public void setupRepository() throws IOException {
    initRepository();
  }

  @Test
  public void testFileExistsWhenFileExists_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt");
    RevCommit commit = commit();
    assertTrue(GitFileUtils.exists("/test_file.txt", commit.getName(), repo));
  }

  @Test
  public void testFileExistsWhenDirectoryExists_shouldReturnTrue() throws IOException {
    writeToCache("/dir/file.txt");
    RevCommit commit = commit();
    assertTrue(GitFileUtils.exists("/dir", commit.getName(), repo));
  }

  @Test
  public void testFileExistsWhenFileDoesNotExist_shouldReturnFalse() throws IOException {
    writeSomethingToCache();
    RevCommit commit = commit();
    assertFalse(GitFileUtils.exists("/non_existent_file.txt", commit.getName(), repo));
  }

  @Test
  public void testIsFileWhenFileExists_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt");
    RevCommit commit = commit();
    assertTrue(GitFileUtils.isFile("/test_file.txt", commit.getName(), repo));
  }

  @Test
  public void testIsFileWhenExecutableFileExists_shouldReturnFalse() throws IOException {
    writeToCache("/test_file.sh", someBytes(), EXECUTABLE_FILE);
    RevCommit commit = commit();
    assertTrue(GitFileUtils.isFile("/test_file.sh", commit.getName(), repo));
  }

  @Test
  public void testIsFileWhenDirectoryExists_shouldReturnFalse() throws IOException {
    writeToCache("/dir/file.txt");
    RevCommit commit = commit();
    assertFalse(GitFileUtils.isFile("/dir", commit.getName(), repo));
  }

  @Test
  public void testIsFileWhenSymbolicLinkExists_shouldReturnFalse() throws IOException {
    writeToCache("/test_file", someBytes(), SYMLINK);
    RevCommit commit = commit();
    assertFalse(GitFileUtils.isFile("/test_file", commit.getName(), repo));
  }

  @Test
  public void testIsDirectoryWhenFileExists_shouldReturnFalse() throws IOException {
    writeToCache("/test_file.txt");
    RevCommit commit = commit();
    assertFalse(GitFileUtils.isDirectory("/test_file.txt", commit.getName(), repo));
  }

  @Test
  public void testIsDirectoryWhenDirectoryExists_shouldReturnFalse() throws IOException {
    writeToCache("/dir/file.txt");
    RevCommit commit = commit();
    assertTrue(GitFileUtils.isDirectory("/dir", commit.getName(), repo));
  }

  @Test
  public void testIsSymbolicLinkWhenFileExists_shouldReturnFalse() throws IOException {
    writeToCache("/test_file.txt");
    RevCommit commit = commit();
    assertFalse(GitFileUtils.isSymbolicLink("/test_file.txt", commit.getName(), repo));
  }

  @Test
  public void testIsSymbolicLinkWhenSymbolicLinkExists_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt", someBytes(), SYMLINK);
    RevCommit commit = commit();
    assertTrue(GitFileUtils.isSymbolicLink("/test_file.txt", commit.getName(), repo));
  }

}
