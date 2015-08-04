package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import org.junit.Assert;
import org.junit.Test;

public class GitDirectoryStreamTest extends AbstractGitFileSystemTest {

  @Test
  public void directoryStreamOfDirectoryTest() throws IOException {
    initRepository();
    String[] files = new String[] {"/a/b.txt", "/a/c/c1.txt", "/a/d/d1.txt", "/a/d/d2.txt", "/a/e.txt", "/f.txt", "/g/h.txt"};
    for(String file : files)
      writeFile(file);
    commitToMaster();
    initGitFileSystem();

    try(DirectoryStream<Path> ds = provider.newDirectoryStream(gfs.getPath("/a"), null)) {
      String[] filesInA = new String[] {"/a/b.txt", "/a/c", "/a/d", "/a/e.txt"};
      Iterator<Path> dsIt = ds.iterator();
      for(String file : filesInA) {
        Assert.assertTrue(dsIt.hasNext());
        Assert.assertEquals(file, dsIt.next().toString());
      }
      Assert.assertFalse(dsIt.hasNext());
    }
  }

  @Test(expected = NoSuchElementException.class)
  public void directoryStreamOfDirectoryNoSuchElementTest() throws IOException {
    initRepository();
    writeFile("a/b");
    commitToMaster();
    initGitFileSystem();
    try(DirectoryStream<Path> ds = Files.newDirectoryStream(gfs.getPath("/a"))) {
      Iterator<Path> dsIt = ds.iterator();
      dsIt.next();
      dsIt.next();
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  public void directoryStreamOfDirectoryRemoveTest() throws IOException {
    initRepository();
    writeFile("a/b");
    commitToMaster();
    initGitFileSystem();
    try(DirectoryStream<Path> ds = Files.newDirectoryStream(gfs.getPath("/a"))) {
      Iterator<Path> dsIt = ds.iterator();
      dsIt.next();
      dsIt.remove();
    }
  }
}
