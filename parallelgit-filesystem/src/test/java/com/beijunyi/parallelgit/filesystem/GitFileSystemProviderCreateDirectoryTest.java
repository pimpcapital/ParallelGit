package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class GitFileSystemProviderCreateDirectoryTest extends AbstractGitFileSystemTest {

  @Test
  public void createNewDirectory_theSpecifiedDirectoryShouldExistAfterTheOperation() throws IOException {
    initGitFileSystem();
    Path dir = gfs.getPath("/dir");
    provider.createDirectory(dir);
    assertTrue(Files.isDirectory(dir));
  }

  @Test
  public void createNewDirectoryAndCreateChildFile_theChildFileShouldExistAfterTheOperation() throws IOException {
    initGitFileSystem();
    Path dir = gfs.getPath("/dir");
    provider.createDirectory(dir);
    Path childFile = dir.resolve("file.txt");
    Files.write(childFile, "some text data".getBytes());
    assertTrue(Files.exists(childFile));
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void createNewDirectoryWhenDirectoryExists_shouldThrowFileAlreadyExistsException() throws IOException {
    initRepository();
    writeToCache("/a/b.txt");
    commitToMaster();
    initGitFileSystem();
    provider.createDirectory(gfs.getPath("/a"));
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void createNewDirectoryWhenFileExists_shouldThrowFileAlreadyExistsException() throws IOException {
    initRepository();
    writeToCache("/a");
    commitToMaster();
    initGitFileSystem();
    provider.createDirectory(gfs.getPath("/a"));
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void createRootDirectory_shouldThrowFileAlreadyExistsException() throws IOException {
    initGitFileSystem();
    provider.createDirectory(gfs.getPath("/"));
  }


}
