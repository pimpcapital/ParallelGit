package com.beijunyi.parallelgit.filesystems;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;

import org.junit.Test;

public class GitFileSystemProviderCreateDirectoryTest extends AbstractGitFileSystemTest {

  /**
   * Git File System does not support empty directory. This test only confirms creating a new directory does not fail.
   */
  @Test
  public void createNewDirectoryTest() throws IOException {
    initGitFileSystem();
    Files.createDirectory(gfs.getPath("/a"));
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void createExistingDirectoryTest() throws IOException {
    initRepository();
    writeFile("a/b.txt");
    commitToMaster();
    initGitFileSystem();
    Files.createDirectory(gfs.getPath("/a"));
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void createDirectoryWithSameNameOfExistingFileTest() throws IOException {
    initRepository();
    writeFile("a");
    commitToMaster();
    initGitFileSystem();
    Files.createDirectory(gfs.getPath("/a"));
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void createDirectoryWithRootPathTest() throws IOException {
    initGitFileSystem();
    Files.createDirectory(gfs.getPath("/"));
  }


}
