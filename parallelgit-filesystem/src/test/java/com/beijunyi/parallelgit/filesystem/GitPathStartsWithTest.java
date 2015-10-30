package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GitPathStartsWithTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void absolutePathStartsWithTest() {
    GitPath path = gfs.getPath("/ab/cd");
    assertTrue(path.startsWith("/"));
    assertTrue(path.startsWith("/ab/cd"));
    assertFalse(path.startsWith("/ab/cd/ef"));
    assertTrue(path.startsWith("/ab"));
    assertTrue(path.startsWith("/ab/"));
    assertFalse(path.startsWith("/cd"));
    assertFalse(path.startsWith("/ab/c"));
    assertFalse(path.startsWith("/a"));
    assertFalse(path.startsWith(""));
    assertFalse(path.startsWith("ab/cd"));
    assertFalse(path.startsWith("ab/cd/ef"));
    assertFalse(path.startsWith("ab"));
    assertFalse(path.startsWith("a"));
    assertFalse(path.startsWith("d"));
    assertFalse(path.startsWith("cd"));
  }

  @Test
  public void relativePathStartsWithTest() {
    GitPath path = gfs.getPath("ab/cd");
    assertFalse(path.startsWith("/"));
    assertFalse(path.startsWith("/ab/cd"));
    assertFalse(path.startsWith("/ab/cd/ef"));
    assertFalse(path.startsWith("/ab"));
    assertFalse(path.startsWith("/ab/"));
    assertFalse(path.startsWith("/cd"));
    assertFalse(path.startsWith("/ab/c"));
    assertFalse(path.startsWith("/a"));
    assertFalse(path.startsWith(""));
    assertTrue(path.startsWith("ab/cd"));
    assertFalse(path.startsWith("ab/cd/ef"));
    assertTrue(path.startsWith("ab"));
    assertFalse(path.startsWith("a"));
    assertFalse(path.startsWith("d"));
    assertFalse(path.startsWith("cd"));
  }

}
