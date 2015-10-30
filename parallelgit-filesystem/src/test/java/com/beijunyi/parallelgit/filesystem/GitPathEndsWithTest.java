package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GitPathEndsWithTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void absolutePathEndsWithTest() {
    GitPath path = gfs.getPath("/ab/cd");
    assertFalse(path.endsWith("/"));
    assertTrue(path.endsWith("/ab/cd"));
    assertFalse(path.endsWith("/ab/cd/ef"));
    assertFalse(path.endsWith("/ab"));
    assertFalse(path.endsWith("/ab/"));
    assertFalse(path.endsWith("/cd"));
    assertFalse(path.endsWith("/ab/c"));
    assertFalse(path.endsWith("/a"));
    assertFalse(path.endsWith(""));
    assertTrue(path.endsWith("ab/cd"));
    assertFalse(path.endsWith("ab/cd/ef"));
    assertFalse(path.endsWith("ab"));
    assertFalse(path.endsWith("a"));
    assertFalse(path.endsWith("d"));
    assertTrue(path.endsWith("cd"));
  }

  @Test
  public void relativePathEndsWithTest() {
    GitPath path = gfs.getPath("ab/cd");
    assertFalse(path.endsWith("/"));
    assertFalse(path.endsWith("/ab/cd"));
    assertFalse(path.endsWith("/ab/cd/ef"));
    assertFalse(path.endsWith("/ab"));
    assertFalse(path.endsWith("/ab/"));
    assertFalse(path.endsWith("/cd"));
    assertFalse(path.endsWith("/ab/c"));
    assertFalse(path.endsWith("/a"));
    assertFalse(path.endsWith(""));
    assertTrue(path.endsWith("ab/cd"));
    assertFalse(path.endsWith("ab/cd/ef"));
    assertFalse(path.endsWith("ab"));
    assertFalse(path.endsWith("a"));
    assertFalse(path.endsWith("d"));
    assertTrue(path.endsWith("cd"));
  }

}
