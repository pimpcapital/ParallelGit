package com.beijunyi.parallelgit.gfs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Test;

public class GitPathTypeTest extends AbstractGitFileSystemTest {

  @Test
  public void rootPathTypeTest() throws IOException {
    initGitFileSystem();
    Assert.assertTrue(Files.isDirectory(root));
    Assert.assertFalse(Files.isRegularFile(root));
  }

  @Test
  public void filePathTypeTest() throws IOException {
    initRepository();
    String file = "a.txt";
    writeFile(file);
    commitToMaster();
    initGitFileSystem();
    Path path = root.resolve(file);
    Assert.assertFalse(Files.isDirectory(path));
    Assert.assertTrue(Files.isRegularFile(path));
  }

  @Test
  public void directoryPathTypeTest() throws IOException {
    initRepository();
    String dir = "a";
    writeFile(dir + "/b.txt");
    commitToMaster();
    initGitFileSystem();
    Path path = root.resolve(dir);
    Assert.assertTrue(Files.isDirectory(path));
    Assert.assertFalse(Files.isRegularFile(path));
  }

  @Test
  public void nonExistentPathTypeTest() throws IOException {
    initGitFileSystem();
    GitPath path = root.resolve("non_existent.txt");
    Assert.assertFalse(Files.isDirectory(path));
    Assert.assertFalse(Files.isRegularFile(path));
  }

  @Test
  public void rootPathTypeInCacheTest() throws IOException {
    initGitFileSystem();
    loadCache();
    Assert.assertTrue(Files.isDirectory(root));
    Assert.assertFalse(Files.isRegularFile(root));
  }

  @Test
  public void filePathTypeInCacheTest() throws IOException {
    initRepository();
    String file = "a.txt";
    writeFile(file);
    commitToMaster();
    initGitFileSystem();
    loadCache();
    Path path = root.resolve(file);
    Assert.assertFalse(Files.isDirectory(path));
    Assert.assertTrue(Files.isRegularFile(path));
  }

  @Test
  public void directoryPathTypeInCacheTest() throws IOException {
    initRepository();
    String dir = "a";
    writeFile(dir + "/b.txt");
    commitToMaster();
    initGitFileSystem();
    loadCache();
    Path path = root.resolve(dir);
    Assert.assertTrue(Files.isDirectory(path));
    Assert.assertFalse(Files.isRegularFile(path));
  }

  @Test
  public void nonExistentPathTypeInCacheTest() throws IOException {
    initGitFileSystem();
    loadCache();
    GitPath path = root.resolve("non_existent.txt");
    Assert.assertFalse(Files.isDirectory(path));
    Assert.assertFalse(Files.isRegularFile(path));
  }


}
