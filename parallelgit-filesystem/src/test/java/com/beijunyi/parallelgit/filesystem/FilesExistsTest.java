package com.beijunyi.parallelgit.filesystem;

import java.nio.file.Files;

import org.junit.Test;

import static org.junit.Assert.*;

public class FilesExistsTest extends AbstractGitFileSystemTest {

  @Test
  public void rootExistsTest() throws Exception {
    initGitFileSystem();
    assertTrue(Files.exists(root));
  }

  @Test
  public void fileExistsTest() throws Exception {
    initRepository();
    String aTxt = "a.txt";
    writeToCache(aTxt);
    String bTxt = "b.txt";
    writeToCache(bTxt);
    commitToMaster();
    initGitFileSystem();

    GitPath aTxtPath = root.resolve(aTxt);
    assertTrue(Files.exists(aTxtPath));
    GitPath bTxtPath = root.resolve(bTxt);
    assertTrue(Files.exists(bTxtPath));
    GitPath nonExistentPath = root.resolve("non_existent.txt");
    assertFalse(Files.exists(nonExistentPath));
  }

  @Test
  public void directoryExistsTest() throws Exception {
    initRepository();
    writeToCache("a/file1.txt");
    writeToCache("b/dir1/file2.txt");
    commitToMaster();
    initGitFileSystem();
    assertTrue(Files.exists(root.resolve("a")));
    assertTrue(Files.exists(root.resolve("b")));
    assertTrue(Files.exists(root.resolve("b/dir1")));
  }

}
