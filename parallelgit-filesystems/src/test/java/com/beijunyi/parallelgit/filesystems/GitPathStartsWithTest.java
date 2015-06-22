package com.beijunyi.parallelgit.filesystems;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GitPathStartsWithTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void absolutePathStartsWithTest() {
    GitPath path = gfs.getPath("/ab/cd");
    Assert.assertTrue(path.startsWith("/"));
    Assert.assertTrue(path.startsWith("/ab/cd"));
    Assert.assertFalse(path.startsWith("/ab/cd/ef"));
    Assert.assertTrue(path.startsWith("/ab"));
    Assert.assertTrue(path.startsWith("/ab/"));
    Assert.assertFalse(path.startsWith("/cd"));
    Assert.assertFalse(path.startsWith("/ab/c"));
    Assert.assertFalse(path.startsWith("/a"));
    Assert.assertFalse(path.startsWith(""));
    Assert.assertFalse(path.startsWith("ab/cd"));
    Assert.assertFalse(path.startsWith("ab/cd/ef"));
    Assert.assertFalse(path.startsWith("ab"));
    Assert.assertFalse(path.startsWith("a"));
    Assert.assertFalse(path.startsWith("d"));
    Assert.assertFalse(path.startsWith("cd"));
  }

  @Test
  public void relativePathStartsWithTest() {
    GitPath path = gfs.getPath("ab/cd");
    Assert.assertFalse(path.startsWith("/"));
    Assert.assertFalse(path.startsWith("/ab/cd"));
    Assert.assertFalse(path.startsWith("/ab/cd/ef"));
    Assert.assertFalse(path.startsWith("/ab"));
    Assert.assertFalse(path.startsWith("/ab/"));
    Assert.assertFalse(path.startsWith("/cd"));
    Assert.assertFalse(path.startsWith("/ab/c"));
    Assert.assertFalse(path.startsWith("/a"));
    Assert.assertFalse(path.startsWith(""));
    Assert.assertTrue(path.startsWith("ab/cd"));
    Assert.assertFalse(path.startsWith("ab/cd/ef"));
    Assert.assertTrue(path.startsWith("ab"));
    Assert.assertFalse(path.startsWith("a"));
    Assert.assertFalse(path.startsWith("d"));
    Assert.assertFalse(path.startsWith("cd"));
  }

}
