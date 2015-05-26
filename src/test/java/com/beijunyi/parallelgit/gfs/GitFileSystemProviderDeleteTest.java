package com.beijunyi.parallelgit.gfs;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Test;

public class GitFileSystemProviderDeleteTest extends AbstractGitFileSystemTest {

  @Test
  public void deleteFileTest() throws IOException {
    initRepository();
    writeFile("a.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath path = gfs.getPath("/a.txt");
    Files.delete(path);
    Assert.assertFalse(Files.exists(path));
  }

  @Test
  public void deleteAllFilesInDirectoryTest() throws IOException {
    initRepository();
    writeFile("a/b1.txt");
    writeFile("a/b2.txt");
    commitToMaster();
    initGitFileSystem();

    Files.delete(gfs.getPath("/a/b1.txt"));
    Files.delete(gfs.getPath("/a/b2.txt"));
    Assert.assertFalse(Files.exists(gfs.getPath("/a")));
  }

  @Test
  public void deleteSomeFilesInDirectoryTest() throws IOException {
    initRepository();
    writeFile("a/b1.txt");
    writeFile("a/b2.txt");
    commitToMaster();
    initGitFileSystem();

    Files.delete(gfs.getPath("/a/b1.txt"));
    Assert.assertTrue(Files.exists(gfs.getPath("/a")));
  }

  @Test
  public void deleteModifiedFileTest() throws IOException {
    initGitFileSystem();

    GitPath path = gfs.getPath("/a.txt");
    Files.write(path, "some data".getBytes());
    Files.delete(path);
    Assert.assertFalse(Files.exists(path));
  }

  @Test(expected = DirectoryNotEmptyException.class)
  public void deleteDirectoryTest() throws IOException {
    initRepository();
    writeFile("a/b.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath path = gfs.getPath("/a");
    Files.delete(path);
  }

}
