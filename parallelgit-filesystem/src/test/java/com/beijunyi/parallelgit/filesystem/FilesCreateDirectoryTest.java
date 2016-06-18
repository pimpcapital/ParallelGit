package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import static org.junit.Assert.*;

public class FilesCreateDirectoryTest extends AbstractGitFileSystemTest {

  @Test
  public void createNewDirectory_theSpecifiedDirectoryShouldExistAfterTheOperation() throws IOException {
    initGitFileSystem();
    Path dir = gfs.getPath("/dir");
    Files.createDirectory(dir);
    assertTrue(Files.isDirectory(dir));
  }

  @Test
  public void createNewDirectoryAndCreateChildFile_theChildFileShouldExistAfterTheOperation() throws IOException {
    initGitFileSystem();
    Path dir = gfs.getPath("/dir");
    Files.createDirectory(dir);
    Path childFile = dir.resolve("file.txt");
    Files.write(childFile, someBytes());
    assertTrue(Files.exists(childFile));
  }

  @Test
  public void createEmptyDirectory_theFileSystemShouldStayClean() throws IOException {
    initGitFileSystem();
    GitPath dir = gfs.getPath("/empty_dir");
    Files.createDirectory(dir);
    assertFalse(gfs.getStatusProvider().isDirty());
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void createNewDirectoryWhenDirectoryExists_shouldThrowFileAlreadyExistsException() throws IOException {
    initGitFileSystem("/a/b.txt");
    Files.createDirectory(gfs.getPath("/a"));
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void createNewDirectoryWhenFileExists_shouldThrowFileAlreadyExistsException() throws IOException {
    initGitFileSystem("/a");
    Files.createDirectory(gfs.getPath("/a"));
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void createRootDirectory_shouldThrowFileAlreadyExistsException() throws IOException {
    initGitFileSystem();
    Files.createDirectory(gfs.getPath("/"));
  }


}
