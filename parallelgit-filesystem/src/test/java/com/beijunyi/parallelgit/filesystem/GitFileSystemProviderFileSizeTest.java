package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

import org.eclipse.jgit.lib.Constants;
import org.junit.Assert;
import org.junit.Test;

public class GitFileSystemProviderFileSizeTest extends AbstractGitFileSystemTest {

  @Test
  public void getRootSizeTest() throws IOException {
    initGitFileSystem();
    Assert.assertEquals(0, Files.size(gfs.getRootPath()));
  }

  @Test
  public void getDirectorySizeTest() throws IOException {
    initRepository();
    writeFile("a/b.txt");
    commitToMaster();
    initGitFileSystem();
    Assert.assertEquals(0, Files.size(gfs.getPath("/a")));
  }

  @Test
  public void getFileSizeTest() throws IOException {
    initRepository();
    byte[] data = Constants.encode("some plain text data");
    writeFile("a.txt", data);
    commitToMaster();
    initGitFileSystem();
    Assert.assertEquals(data.length, Files.size(gfs.getPath("/a.txt")));
  }

  @Test(expected = NoSuchFileException.class)
  public void getNonExistentFileSizeTest() throws IOException {
    initGitFileSystem();
    Files.size(gfs.getPath("/a.txt"));
  }

  @Test
  public void getRootSizeInCacheTest() throws IOException {
    initGitFileSystem();
    loadCache();
    Assert.assertEquals(0, Files.size(gfs.getRootPath()));
  }

  @Test
  public void getDirectorySizeInCacheTest() throws IOException {
    initRepository();
    writeFile("a/b.txt");
    commitToMaster();
    initGitFileSystem();
    loadCache();
    Assert.assertEquals(0, Files.size(gfs.getPath("/a")));
  }

  @Test
  public void getFileSizeInCacheTest() throws IOException {
    initRepository();
    byte[] data = Constants.encode("some plain text data");
    writeFile("a.txt", data);
    commitToMaster();
    initGitFileSystem();
    loadCache();
    Assert.assertEquals(data.length, Files.size(gfs.getPath("/a.txt")));
  }

  @Test
  public void getSizeOfFileAttachedToMemoryChannelTest() throws IOException {
    initRepository();
    writeFile("a.txt");
    commitToMaster();
    initGitFileSystem();
    GitPath path = gfs.getPath("/a.txt");
    byte[] data = Constants.encode("some plain text data");
    Files.write(path, data);
    Assert.assertEquals(data.length, Files.size(path));
  }

}
