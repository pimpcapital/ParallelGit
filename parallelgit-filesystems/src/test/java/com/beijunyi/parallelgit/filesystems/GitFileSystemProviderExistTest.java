package com.beijunyi.parallelgit.filesystems;

import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Test;

public class GitFileSystemProviderExistTest extends AbstractGitFileSystemTest {

  @Test
  public void rootExistsTest() throws Exception {
    initGitFileSystem();
    Assert.assertTrue(Files.exists(root));
  }

  @Test
  public void fileExistsTest() throws Exception {
    initRepository();
    String aTxt = "a.txt";
    writeFile(aTxt);
    String bTxt = "b.txt";
    writeFile(bTxt);
    commitToMaster();
    initGitFileSystem();

    GitPath aTxtPath = root.resolve(aTxt);
    Assert.assertTrue(Files.exists(aTxtPath));
    GitPath bTxtPath = root.resolve(bTxt);
    Assert.assertTrue(Files.exists(bTxtPath));
    GitPath nonExistentPath = root.resolve("non_existent.txt");
    Assert.assertFalse(Files.exists(nonExistentPath));
  }

  @Test
  public void directoryExistsTest() throws Exception {
    initRepository();
    writeFile("a/file1.txt");
    writeFile("b/dir1/file2.txt");
    commitToMaster();
    initGitFileSystem();
    Assert.assertTrue(Files.exists(root.resolve("a")));
    Assert.assertTrue(Files.exists(root.resolve("b")));
    Assert.assertTrue(Files.exists(root.resolve("b/dir1")));
  }

}
