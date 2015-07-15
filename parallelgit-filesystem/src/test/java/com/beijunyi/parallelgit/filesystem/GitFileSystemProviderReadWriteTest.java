package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

import org.eclipse.jgit.lib.Constants;
import org.junit.Assert;
import org.junit.Test;

public class GitFileSystemProviderReadWriteTest extends AbstractGitFileSystemTest {

  @Test
  public void readFileTest() throws IOException {
    initRepository();
    byte[] data = Constants.encode("some plain text data");
    String file = "a.txt";
    writeFile(file, data);
    commitToMaster();
    initGitFileSystem();
    GitPath path = root.resolve(file);
    Assert.assertArrayEquals(data, Files.readAllBytes(path));
  }

  @Test
  public void writeFileTest() throws IOException {
    initRepository();
    String file = "a.txt";
    writeFile(file, "old content");
    commitToMaster();
    initGitFileSystem();
    GitPath path = root.resolve(file);
    byte[] data = Constants.encode("some plain text data");
    Files.write(path, data);
    Assert.assertArrayEquals(data, Files.readAllBytes(path));
  }

  @Test
  public void writeNewFileTest() throws IOException {
    initGitFileSystem();
    GitPath path = root.resolve("a.txt");
    Files.write(path, Constants.encode("some plain text data"));
    Assert.assertTrue(Files.exists(path));
  }

  @Test
  public void readNewFileTest() throws IOException {
    initGitFileSystem();
    byte[] data = Constants.encode("some plain text data");
    GitPath path = root.resolve("a.txt");
    Files.write(path, data);
    Assert.assertArrayEquals(data, Files.readAllBytes(path));
  }

  @Test(expected = NoSuchFileException.class)
  public void readFromNonExistentFileTest() throws IOException {
    initGitFileSystem();
    Files.readAllBytes(root.resolve("a.txt"));
  }

  @Test(expected = AccessDeniedException.class)
  public void readFromRootTest() throws IOException {
    initGitFileSystem();
    Files.readAllBytes(root);
  }

  @Test(expected = AccessDeniedException.class)
  public void readFromDirectoryTest() throws IOException {
    initRepository();
    String dir = "a";
    writeFile(dir + "/b.txt", "some text");
    commitToMaster();
    initGitFileSystem();
    GitPath path = root.resolve(dir);
    Files.readAllBytes(path);
  }

  @Test(expected = NoSuchFileException.class)
  public void readFromNonExistentFileInCacheTest() throws IOException {
    initGitFileSystem();
    loadCache();
    Files.readAllBytes(root.resolve("a.txt"));
  }

  @Test(expected = AccessDeniedException.class)
  public void readFromRootInCacheTest() throws IOException {
    initGitFileSystem();
    loadCache();
    Files.readAllBytes(root);
  }

  @Test(expected = AccessDeniedException.class)
  public void readFromDirectoryInCacheTest() throws IOException {
    initRepository();
    String dir = "a";
    writeFile(dir + "/b.txt", "some text");
    commitToMaster();
    initGitFileSystem();
    loadCache();
    GitPath path = root.resolve(dir);
    Files.readAllBytes(path);
  }

}
