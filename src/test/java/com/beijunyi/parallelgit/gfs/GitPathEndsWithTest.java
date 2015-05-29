package com.beijunyi.parallelgit.gfs;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GitPathEndsWithTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
    preventDestroyFileSystem();
  }

  @Test
  public void absolutePathEndsWithTest() {
    GitPath path = gfs.getPath("/ab/cd");
    Assert.assertFalse(path.endsWith("/"));
    Assert.assertTrue(path.endsWith("/ab/cd"));
    Assert.assertFalse(path.endsWith("/ab/cd/ef"));
    Assert.assertFalse(path.endsWith("/ab"));
    Assert.assertFalse(path.endsWith("/ab/"));
    Assert.assertFalse(path.endsWith("/cd"));
    Assert.assertFalse(path.endsWith("/ab/c"));
    Assert.assertFalse(path.endsWith("/a"));
    Assert.assertFalse(path.endsWith(""));
    Assert.assertTrue(path.endsWith("ab/cd"));
    Assert.assertFalse(path.endsWith("ab/cd/ef"));
    Assert.assertFalse(path.endsWith("ab"));
    Assert.assertFalse(path.endsWith("a"));
    Assert.assertFalse(path.endsWith("d"));
    Assert.assertTrue(path.endsWith("cd"));
  }

  @Test
  public void relativePathEndsWithTest() {
    GitPath path = gfs.getPath("ab/cd");
    Assert.assertFalse(path.endsWith("/"));
    Assert.assertFalse(path.endsWith("/ab/cd"));
    Assert.assertFalse(path.endsWith("/ab/cd/ef"));
    Assert.assertFalse(path.endsWith("/ab"));
    Assert.assertFalse(path.endsWith("/ab/"));
    Assert.assertFalse(path.endsWith("/cd"));
    Assert.assertFalse(path.endsWith("/ab/c"));
    Assert.assertFalse(path.endsWith("/a"));
    Assert.assertFalse(path.endsWith(""));
    Assert.assertTrue(path.endsWith("ab/cd"));
    Assert.assertFalse(path.endsWith("ab/cd/ef"));
    Assert.assertFalse(path.endsWith("ab"));
    Assert.assertFalse(path.endsWith("a"));
    Assert.assertFalse(path.endsWith("d"));
    Assert.assertTrue(path.endsWith("cd"));
  }

}
