package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Test;

public class GitFileSystemUtilsFastDeleteDirectoryTest extends AbstractGitFileSystemTest {

  @Test
  public void fastDeleteRootTest() throws IOException {
    initRepository();
    writeFile("a/a1.txt");
    writeFile("a/a2.txt");
    writeFile("b/b1.txt");
    commitToMaster();
    initGitFileSystem();

    GitFileSystemUtils.fastDeleteDirectory(gfs.getRoot());
    Assert.assertFalse(Files.exists(gfs.getPath("/a/a1.txt")));
    Assert.assertFalse(Files.exists(gfs.getPath("/a/a2.txt")));
    Assert.assertFalse(Files.exists(gfs.getPath("/b/b1.txt")));
  }

  @Test
  public void fastDeleteDirectoryTest() throws IOException {
    initRepository();
    writeFile("a/a1.txt");
    writeFile("a/a2.txt");
    writeFile("b/b1.txt");
    commitToMaster();
    initGitFileSystem();

    GitFileSystemUtils.fastDeleteDirectory(gfs.getPath("/a"));
    Assert.assertFalse(Files.exists(gfs.getPath("/a/a1.txt")));
    Assert.assertFalse(Files.exists(gfs.getPath("/a/a2.txt")));
    Assert.assertTrue(Files.exists(gfs.getPath("/b/b1.txt")));
  }
}
