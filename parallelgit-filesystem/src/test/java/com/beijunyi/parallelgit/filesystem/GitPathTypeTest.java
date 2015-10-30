package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import static org.junit.Assert.*;

public class GitPathTypeTest extends AbstractGitFileSystemTest {

  @Test
  public void rootPathTypeTest() throws IOException {
    initGitFileSystem();
    assertTrue(Files.isDirectory(root));
    assertFalse(Files.isRegularFile(root));
  }

  @Test
  public void filePathTypeTest() throws IOException {
    initRepository();
    String file = "a.txt";
    writeToCache(file);
    commitToMaster();
    initGitFileSystem();
    Path path = root.resolve(file);
    assertFalse(Files.isDirectory(path));
    assertTrue(Files.isRegularFile(path));
  }

  @Test
  public void directoryPathTypeTest() throws IOException {
    initRepository();
    String dir = "a";
    writeToCache(dir + "/b.txt");
    commitToMaster();
    initGitFileSystem();
    Path path = root.resolve(dir);
    assertTrue(Files.isDirectory(path));
    assertFalse(Files.isRegularFile(path));
  }

  @Test
  public void nonExistentPathTypeTest() throws IOException {
    initGitFileSystem();
    GitPath path = root.resolve("non_existent.txt");
    assertFalse(Files.isDirectory(path));
    assertFalse(Files.isRegularFile(path));
  }
}
