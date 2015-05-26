package com.beijunyi.parallelgit.gfs;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GitFileSystemProviderIsSameFileTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() {
    initGitFileSystem();
    preventDestroyFileSystem();
  }

  @Test
  public void sameAbsolutePathsIsSameFileTest() throws IOException {
    GitPath p1 = gfs.getPath("/a/b");
    GitPath p2 = gfs.getPath("/a/b");
    Assert.assertTrue(Files.isSameFile(p1, p2));
  }

  @Test
  public void absolutePathsWithDotsIsSameFileTest() throws IOException {
    GitPath p1 = gfs.getPath("/a/b");
    GitPath p2 = gfs.getPath("/a/b/.");
    GitPath p3 = gfs.getPath("/a/./b");
    Assert.assertTrue(Files.isSameFile(p1, p2));
    Assert.assertTrue(Files.isSameFile(p1, p3));
    Assert.assertTrue(Files.isSameFile(p2, p3));
  }

  @Test
  public void absolutePathsWithDoubleDotsIsSameFileTest() throws IOException {
    GitPath p1 = gfs.getPath("/a/b.");
    GitPath p2 = gfs.getPath("/a/../a/b");
    GitPath p3 = gfs.getPath("/a/b/../b");
    GitPath p4 = gfs.getPath("/a/b/c/..");
    Assert.assertTrue(Files.isSameFile(p1, p2));
    Assert.assertTrue(Files.isSameFile(p1, p3));
    Assert.assertTrue(Files.isSameFile(p1, p4));
    Assert.assertTrue(Files.isSameFile(p2, p3));
    Assert.assertTrue(Files.isSameFile(p2, p4));
    Assert.assertTrue(Files.isSameFile(p3, p4));
  }

  @Test
  public void sameRelativePathsIsSameFileTest() throws IOException {
    GitPath p1 = gfs.getPath("a/b");
    GitPath p2 = gfs.getPath("a/b");
    Assert.assertTrue(Files.isSameFile(p1, p2));
  }

  @Test
  public void relativePathsWithDotsIsSameFileTest() throws IOException {
    GitPath p1 = gfs.getPath("a/b");
    GitPath p2 = gfs.getPath("a/b/.");
    GitPath p3 = gfs.getPath("a/./b");
    Assert.assertTrue(Files.isSameFile(p1, p2));
    Assert.assertTrue(Files.isSameFile(p1, p3));
    Assert.assertTrue(Files.isSameFile(p2, p3));
  }

  @Test
  public void relativePathsWithDoubleDotsIsSameFileTest() throws IOException {
    GitPath p1 = gfs.getPath("a/b.");
    GitPath p2 = gfs.getPath("a/../a/b");
    GitPath p3 = gfs.getPath("a/b/../b");
    GitPath p4 = gfs.getPath("a/b/c/..");
    Assert.assertTrue(Files.isSameFile(p1, p2));
    Assert.assertTrue(Files.isSameFile(p1, p3));
    Assert.assertTrue(Files.isSameFile(p1, p4));
    Assert.assertTrue(Files.isSameFile(p2, p3));
    Assert.assertTrue(Files.isSameFile(p2, p4));
    Assert.assertTrue(Files.isSameFile(p3, p4));
  }

  @Test
  public void mixAbsoluteAndRelativesPathIsSameFileTest() throws IOException {
    GitPath p1 = gfs.getPath("/a/b");
    GitPath p2 = gfs.getPath("a/b");
    Assert.assertTrue(Files.isSameFile(p1, p2));
  }
}
