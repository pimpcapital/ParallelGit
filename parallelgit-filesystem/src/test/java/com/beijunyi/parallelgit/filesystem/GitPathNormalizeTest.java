package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GitPathNormalizeTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void normalizesAbsolutePathTest() {
    GitPath path = gfs.getPath("/a/b");
    GitPath result = path.normalize();
    Assert.assertEquals("/a/b", result.toString());
  }

  @Test
  public void normalizesAbsolutePathWithDotsTest() {
    GitPath path = gfs.getPath("/a/./b/.");
    GitPath result = path.normalize();
    Assert.assertEquals("/a/b", result.toString());
  }

  @Test
  public void normalizesAbsolutePathWithJustDotsTest() {
    GitPath path = gfs.getPath("/./.");
    GitPath result = path.normalize();
    Assert.assertEquals("/", result.toString());
  }

  @Test
  public void normalizesAbsolutePathWithDoubleDotsTest() {
    GitPath path = gfs.getPath("/a/c/../b/../b");
    GitPath result = path.normalize();
    Assert.assertEquals("/a/b", result.toString());
  }

  @Test
  public void normalizesAbsolutePathWithMoreDoubleDotsThanNamesTest() {
    GitPath path = gfs.getPath("/a/../..");
    GitPath result = path.normalize();
    Assert.assertEquals("/", result.toString());
  }

  @Test
  public void normalizesAbsolutePathWithJustDoubleDotsTest() {
    GitPath path = gfs.getPath("/../..");
    GitPath result = path.normalize();
    Assert.assertEquals("/", result.toString());
  }

  @Test
  public void normalizesAbsolutePathWithDotsAndDoubleDotsTest() {
    GitPath path = gfs.getPath("/./a/./c/../b/../b");
    GitPath result = path.normalize();
    Assert.assertEquals("/a/b", result.toString());
  }

  @Test
  public void normalizesRootPathTest() {
    GitPath path = gfs.getPath("/");
    GitPath result = path.normalize();
    Assert.assertEquals("/", result.toString());
  }

  @Test
  public void normalizesRelativePathTest() {
    GitPath path = gfs.getPath("a/b");
    GitPath result = path.normalize();
    Assert.assertEquals("a/b", result.toString());
  }

  @Test
  public void normalizesRelativePathWithDotsTest() {
    GitPath path = gfs.getPath("a/./b/.");
    GitPath result = path.normalize();
    Assert.assertEquals("a/b", result.toString());
  }

  @Test
  public void normalizesRelativePathWithJustDotsTest() {
    GitPath path = gfs.getPath("./.");
    GitPath result = path.normalize();
    Assert.assertEquals("", result.toString());
  }

  @Test
  public void normalizesRelativePathWithDoubleDotsTest() {
    GitPath path = gfs.getPath("a/c/../b/../b");
    GitPath result = path.normalize();
    Assert.assertEquals("a/b", result.toString());
  }

  @Test
  public void normalizesRelativePathWithMoreDoubleDotsThanNamesTest() {
    GitPath path = gfs.getPath("a/../..");
    GitPath result = path.normalize();
    Assert.assertEquals("..", result.toString());
  }

  @Test
  public void normalizesRelativePathWithJustDoubleDotsTest() {
    GitPath path = gfs.getPath("../..");
    GitPath result = path.normalize();
    Assert.assertEquals("../..", result.toString());
  }

  @Test
  public void normalizesRelativePathWithDotsAndDoubleDotsTest() {
    GitPath path = gfs.getPath("./a/./c/../b/../b");
    GitPath result = path.normalize();
    Assert.assertEquals("a/b", result.toString());
  }

  @Test
  public void normalizesEmptyPathTest() {
    GitPath path = gfs.getPath("");
    GitPath result = path.normalize();
    Assert.assertEquals("", result.toString());
  }
}
