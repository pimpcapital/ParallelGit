package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class FilesIsSameFileTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void sameAbsolutePathsIsSameFileTest() throws IOException {
    GitPath p1 = gfs.getPath("/a/b");
    GitPath p2 = gfs.getPath("/a/b");
    assertTrue(Files.isSameFile(p1, p2));
  }

  @Test
  public void absolutePathsWithDotsIsSameFileTest() throws IOException {
    GitPath p1 = gfs.getPath("/a/b");
    GitPath p2 = gfs.getPath("/a/b/.");
    GitPath p3 = gfs.getPath("/a/./b");
    assertTrue(Files.isSameFile(p1, p2));
    assertTrue(Files.isSameFile(p1, p3));
    assertTrue(Files.isSameFile(p2, p3));
  }

  @Test
  public void absolutePathsWithDoubleDotsIsSameFileTest() throws IOException {
    GitPath p1 = gfs.getPath("/a/b.");
    GitPath p2 = gfs.getPath("/a/../a/b");
    GitPath p3 = gfs.getPath("/a/b/../b");
    GitPath p4 = gfs.getPath("/a/b/c/..");
    assertTrue(Files.isSameFile(p1, p2));
    assertTrue(Files.isSameFile(p1, p3));
    assertTrue(Files.isSameFile(p1, p4));
    assertTrue(Files.isSameFile(p2, p3));
    assertTrue(Files.isSameFile(p2, p4));
    assertTrue(Files.isSameFile(p3, p4));
  }

  @Test
  public void sameRelativePathsIsSameFileTest() throws IOException {
    GitPath p1 = gfs.getPath("a/b");
    GitPath p2 = gfs.getPath("a/b");
    assertTrue(Files.isSameFile(p1, p2));
  }

  @Test
  public void relativePathsWithDotsIsSameFileTest() throws IOException {
    GitPath p1 = gfs.getPath("a/b");
    GitPath p2 = gfs.getPath("a/b/.");
    GitPath p3 = gfs.getPath("a/./b");
    assertTrue(Files.isSameFile(p1, p2));
    assertTrue(Files.isSameFile(p1, p3));
    assertTrue(Files.isSameFile(p2, p3));
  }

  @Test
  public void relativePathsWithDoubleDotsIsSameFileTest() throws IOException {
    GitPath p1 = gfs.getPath("a/b.");
    GitPath p2 = gfs.getPath("a/../a/b");
    GitPath p3 = gfs.getPath("a/b/../b");
    GitPath p4 = gfs.getPath("a/b/c/..");
    assertTrue(Files.isSameFile(p1, p2));
    assertTrue(Files.isSameFile(p1, p3));
    assertTrue(Files.isSameFile(p1, p4));
    assertTrue(Files.isSameFile(p2, p3));
    assertTrue(Files.isSameFile(p2, p4));
    assertTrue(Files.isSameFile(p3, p4));
  }

  @Test
  public void mixAbsoluteAndRelativesPathIsSameFileTest() throws IOException {
    GitPath p1 = gfs.getPath("/a/b");
    GitPath p2 = gfs.getPath("a/b");
    assertTrue(Files.isSameFile(p1, p2));
  }
}
