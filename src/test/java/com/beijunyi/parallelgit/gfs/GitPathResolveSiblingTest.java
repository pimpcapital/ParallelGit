package com.beijunyi.parallelgit.gfs;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GitPathResolveSiblingTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void absolutePathResolveSiblingTest() {
    GitPath path = gfs.getPath("/a/b/c");
    GitPath result = path.resolveSibling("d");
    Assert.assertEquals("/a/b/d", result.toString());
  }

  @Test
  public void absolutePathResolveSiblingWithSameFileNameTest() {
    GitPath path = gfs.getPath("/a/b/c");
    GitPath result = path.resolveSibling("c");
    Assert.assertEquals("/a/b/c", result.toString());
  }

  @Test
  public void absolutePathResolveSiblingWithAbsolutePathTest() {
    GitPath path = gfs.getPath("/a/b/c");
    GitPath result = path.resolveSibling("/a/d");
    Assert.assertEquals("/a/d", result.toString());
  }

  @Test
  public void rootPathResolveSiblingTest() {
    GitPath path = gfs.getPath("/");
    GitPath result = path.resolveSibling("d");
    Assert.assertEquals("d", result.toString());
  }

  @Test
  public void relativePathResolveSiblingTest() {
    GitPath path = gfs.getPath("a/b/c");
    GitPath result = path.resolveSibling("d");
    Assert.assertEquals("a/b/d", result.toString());
  }

  @Test
  public void relativePathResolveSiblingWithSameFileNameTest() {
    GitPath path = gfs.getPath("a/b/c");
    GitPath result = path.resolveSibling("c");
    Assert.assertEquals("a/b/c", result.toString());
  }

  @Test
  public void relativePathResolveSiblingWithAbsolutePathTest() {
    GitPath path = gfs.getPath("a/b/c");
    GitPath result = path.resolveSibling("/a/d");
    Assert.assertEquals("/a/d", result.toString());
  }

  @Test
  public void emptyPathResolveSiblingTest() {
    GitPath path = gfs.getPath("");
    GitPath result = path.resolveSibling("d");
    Assert.assertEquals("d", result.toString());
  }

}
