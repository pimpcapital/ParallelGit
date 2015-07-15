package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.*;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;

public class GitFileSystemProviderDirectoryStreamTest extends AbstractGitFileSystemTest {

  @Test
  public void directoryStreamOfDirectoryTest() throws IOException {
    initRepository();
    String[] files = new String[] {"a/b.txt", "a/c/c1.txt", "a/d/d1.txt", "a/d/d2.txt", "a/e.txt", "f.txt", "g/h.txt"};
    for(String file : files)
      writeFile(file);
    commitToMaster();
    initGitFileSystem();

    try(DirectoryStream<Path> ds = Files.newDirectoryStream(gfs.getPath("/a"))) {
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

  @Test(expected = NotDirectoryException.class)
  public void directoryStreamOfFileTest() throws IOException {
    initRepository();
    writeFile("a/b");
    commitToMaster();
    initGitFileSystem();
    Files.newDirectoryStream(gfs.getPath("/a.txt"));
  }

  @Test(expected = NotDirectoryException.class)
  public void directoryStreamOfNonExistentEntryTest() throws IOException {
    initGitFileSystem();
    Files.newDirectoryStream(gfs.getPath("/a"));
  }

  @Test
  public void directoryStreamOfDirectoryInCacheTest() throws IOException {
    initRepository();
    String[] files = new String[] {"a/b.txt", "a/c/c1.txt", "a/d/d1.txt", "a/d/d2.txt", "a/e.txt", "f.txt", "g/h.txt"};
    for(String file : files)
      writeFile(file);
    commitToMaster();
    initGitFileSystem();
    loadCache();

    try(DirectoryStream<Path> ds = Files.newDirectoryStream(gfs.getPath("/a"))) {
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
  public void directoryStreamOfDirectoryInCacheNoSuchElementTest() throws IOException {
    initRepository();
    writeFile("a/b");
    commitToMaster();
    initGitFileSystem();
    loadCache();
    try(DirectoryStream<Path> ds = Files.newDirectoryStream(gfs.getPath("/a"))) {
      Iterator<Path> dsIt = ds.iterator();
      dsIt.next();
      dsIt.next();
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  public void directoryStreamOfDirectoryInCacheRemoveTest() throws IOException {
    initRepository();
    writeFile("a/b");
    commitToMaster();
    initGitFileSystem();
    loadCache();
    try(DirectoryStream<Path> ds = Files.newDirectoryStream(gfs.getPath("/a"))) {
      Iterator<Path> dsIt = ds.iterator();
      dsIt.next();
      dsIt.remove();
    }
  }

  @Test(expected = NotDirectoryException.class)
  public void directoryStreamOfFileInCacheTest() throws IOException {
    initRepository();
    writeFile("a.txt");
    commitToMaster();
    initGitFileSystem();
    loadCache();
    Files.newDirectoryStream(gfs.getPath("/a.txt"));
  }

  @Test(expected = NotDirectoryException.class)
  public void directoryStreamOfNonExistentEntryInCacheTest() throws IOException {
    initGitFileSystem();
    loadCache();
    Files.newDirectoryStream(gfs.getPath("/a"));
  }

}
