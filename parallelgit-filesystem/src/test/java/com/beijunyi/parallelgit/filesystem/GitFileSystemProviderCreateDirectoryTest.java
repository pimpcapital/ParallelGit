package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;

import org.junit.Test;

public class GitFileSystemProviderCreateDirectoryTest extends AbstractGitFileSystemTest {

  @Test
  public void createNewDirectoryTest() throws IOException {
    initGitFileSystem();
    provider.createDirectory(gfs.getPath("/a"));
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void createExistingDirectoryTest() throws IOException {
    initRepository();
    writeFile("/a/b.txt");
    commitToMaster();
    initGitFileSystem();
    provider.createDirectory(gfs.getPath("/a"));
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void createDirectoryWithSameNameOfExistingFileTest() throws IOException {
    initRepository();
    writeFile("/a");
    commitToMaster();
    initGitFileSystem();
    provider.createDirectory(gfs.getPath("/a"));
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void createDirectoryWithRootPathTest() throws IOException {
    initGitFileSystem();
    provider.createDirectory(gfs.getPath("/"));
  }


}
